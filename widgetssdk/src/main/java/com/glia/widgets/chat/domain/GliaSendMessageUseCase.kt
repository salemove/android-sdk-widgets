package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.internal.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.engagement.domain.IsOperatorPresentUseCase

internal class GliaSendMessageUseCase(
    private val chatRepository: GliaChatRepository,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val isOperatorPresentUseCase: IsOperatorPresentUseCase,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val shouldUseSecureMessagingApis: ManageSecureMessagingStatusUseCase
) {
    interface Listener {
        fun messageSent(message: VisitorMessage?)
        fun onMessageValidated()
        fun onMessagePrepared(visitorChatItem: VisitorChatItem, payload: SendMessagePayload)
        fun onAttachmentsPrepared(items: List<VisitorAttachmentItem>, payload: SendMessagePayload?)
        fun errorOperatorOffline(messageId: String)
        fun error(ex: GliaException, messageId: String)
    }

    private val isSecureEngagement: Boolean
        get() = shouldUseSecureMessagingApis.shouldUseSecureMessagingEndpoints

    private fun sendMessage(payload: SendMessagePayload, listener: Listener) {
        if (isSecureEngagement) {
            secureConversationsRepository.send(payload, listener)
        } else {
            chatRepository.sendMessage(payload, listener)
        }
    }

    fun execute(message: String, listener: Listener) {
        val localAttachments: List<LocalAttachment>? = fileAttachmentRepository
            .getReadyToSendFileAttachments()
            .filter { it.engagementFile != null }
            .takeIf { it.isNotEmpty() }

        if (message.isNotBlank() || localAttachments != null) {
            listener.onMessageValidated()

            val messageAttachments = localAttachments
                ?.map { it.engagementFile!! }
                ?.run { FilesAttachment.from(toTypedArray()) }

            val payload = SendMessagePayload(content = message, messageAttachments)

            if (message.isNotBlank()) {
                listener.onMessagePrepared(VisitorMessageItem(message, payload.messageId), payload)
            }

            localAttachments?.map { it.toVisitorAttachmentItem(payload.messageId) }?.also {
                listener.onAttachmentsPrepared(it, payload.takeIf { message.isBlank() })
            }

            if (isOperatorOnline || isSecureEngagement) {
                sendMessage(payload, listener)
            } else {
                listener.errorOperatorOffline(payload.messageId)
            }

            fileAttachmentRepository.detachFiles(localAttachments ?: return)
        }
    }

    fun execute(singleChoiceAttachment: SingleChoiceAttachment, listener: Listener) {
        val payload = SendMessagePayload(attachment = singleChoiceAttachment)
        val messageItem = VisitorMessageItem(payload.content, payload.messageId)
        listener.onMessagePrepared(messageItem, payload)
        when {
            isSecureEngagement -> secureConversationsRepository.send(payload, listener)

            isOperatorOnline -> chatRepository.sendMessage(payload, listener)

            else -> listener.errorOperatorOffline(payload.messageId)
        }
    }

    private val isOperatorOnline: Boolean
        get() = isOperatorPresentUseCase()
}
