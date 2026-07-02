package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.model.OutgoingMessage
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.engagement.domain.IsOperatorPresentUseCase
import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.internal.secureconversations.domain.ManageSecureMessagingStatusUseCase

/**
 * Use case for sending visitor chat input as [OutgoingMessage]s.
 *
 * Text and each ready-to-send file attachment are sent as separate messages: the text (when
 * non-blank) as [OutgoingMessage.Text] and every attachment as its own [OutgoingMessage.File].
 * Each message is announced to the [Listener] for optimistic rendering before it is routed to
 * the live chat or secure messaging endpoints; when neither is available, the listener is
 * notified that the operator is offline so the message can be queued as unsent.
 */
internal class GliaSendMessageUseCase(
    private val chatRepository: GliaChatRepository,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val isOperatorPresentUseCase: IsOperatorPresentUseCase,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val shouldUseSecureMessagingApis: ManageSecureMessagingStatusUseCase
) {
    interface Listener {
        fun messageSent(messageId: String)
        fun onMessageValidated()
        fun onMessagePrepared(visitorChatItem: VisitorChatItem, payload: OutgoingMessage)
        fun onAttachmentPrepared(attachment: VisitorAttachmentItem, outgoingMessage: OutgoingMessage)
        fun errorOperatorOffline(messageId: String)
        fun error(ex: GliaException, messageId: String)
    }

    private val isSecureEngagement: Boolean
        get() = shouldUseSecureMessagingApis.shouldUseSecureMessagingEndpoints

    fun execute(message: String, listener: Listener) {
        val localAttachments = fileAttachmentRepository
            .getReadyToSendFileAttachments()
            .filter { it.engagementFile != null }

        val attachments: List<VisitorAttachmentItem> = localAttachments
            .mapNotNull { it.toVisitorAttachmentItem() }

        if (message.isNotBlank()) {
            listener.onMessageValidated()
            val messagePayload = OutgoingMessage.Text(message)
            listener.onMessagePrepared(VisitorMessageItem(message, messagePayload.messageId), messagePayload)

            val onSuccess: () -> Unit = {
                listener.messageSent(messagePayload.messageId)
            }

            val onFailure: (GliaException) -> Unit = {
                listener.error(it, messagePayload.messageId)
            }

            when {
                isOperatorOnline -> chatRepository.sendMessage(messagePayload.content, messagePayload.messageId, onSuccess, onFailure)
                isSecureEngagement -> secureConversationsRepository.sendMessage(
                    messagePayload.content,
                    messagePayload.messageId,
                    onSuccess,
                    onFailure
                )

                else -> listener.errorOperatorOffline(messagePayload.messageId)
            }
        }

        attachments.forEach {
            val filePayload = OutgoingMessage.File(it.id)
            listener.onAttachmentPrepared(it, filePayload)

            val onSuccess: () -> Unit = {
                listener.messageSent(filePayload.messageId)
            }

            val onFailure: (GliaException) -> Unit = {
                listener.error(it, filePayload.messageId)
            }

            when {
                isOperatorOnline -> chatRepository.sendFileAttachment(filePayload.fileId, onSuccess, onFailure)
                isSecureEngagement -> secureConversationsRepository.sendFileAttachment(filePayload.fileId, onSuccess, onFailure)
                else -> listener.errorOperatorOffline(filePayload.messageId)
            }
        }

        fileAttachmentRepository.detachFiles(localAttachments)
    }

    fun execute(singleChoiceAttachment: SingleChoiceAttachment, listener: Listener) {
        val payload = OutgoingMessage.SingleChoice(singleChoiceAttachment)
        val messageItem = VisitorMessageItem(singleChoiceAttachment.selectedOptionText, payload.messageId)
        listener.onMessagePrepared(messageItem, payload)

        val onSuccess: () -> Unit = {
            listener.messageSent(payload.messageId)
        }

        val onFailure: (GliaException) -> Unit = {
            listener.error(it, payload.messageId)
        }

        when {
            isSecureEngagement -> secureConversationsRepository.sendSingleChoiceAttachment(
                singleChoiceAttachment,
                payload.messageId,
                onSuccess,
                onFailure
            )

            isOperatorOnline -> chatRepository.sendSingleChoiceAttachment(singleChoiceAttachment, payload.messageId, onSuccess, onFailure)

            else -> listener.errorOperatorOffline(payload.messageId)
        }
    }

    private val isOperatorOnline: Boolean
        get() = isOperatorPresentUseCase()
}
