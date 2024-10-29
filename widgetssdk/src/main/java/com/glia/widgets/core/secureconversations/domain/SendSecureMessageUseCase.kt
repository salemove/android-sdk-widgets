package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.SendMessageRepository
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase

internal class SendSecureMessageUseCase(
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val sendMessageRepository: SendMessageRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val fileAttachmentRepository: SecureFileAttachmentRepository,
    private val chatRepository: GliaChatRepository,
    private val isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase
) {

    private val hasOngoingEngagement: Boolean
        get() = isQueueingOrEngagementUseCase.hasOngoingEngagement

    operator fun invoke(
        callback: RequestCallback<VisitorMessage?>
    ) {
        val message = sendMessageRepository.value
        val fileAttachments = fileAttachmentRepository.getReadyToSendFileAttachments()
        sendMessage(message, engagementConfigRepository.queueIds, fileAttachments, callback)
    }

    private fun sendMessage(
        message: String,
        queueIds: List<String>,
        localAttachments: List<LocalAttachment>,
        callback: RequestCallback<VisitorMessage?>
    ) {
        if (localAttachments.isNotEmpty()) {
            sendMessageWithAttachments(message, queueIds, localAttachments) { result, ex ->
                if (ex == null) {
                    sendMessageRepository.reset()
                    fileAttachmentRepository.detachFiles(localAttachments)
                }
                callback.onResult(result, ex)
            }
        } else {
            sendMessage(message, queueIds, callback)
        }
    }

    private fun sendMessage(
        message: String,
        queueIds: List<String>,
        callback: RequestCallback<VisitorMessage?>
    ) {
        val payload = SendMessagePayload(content = message)

        if (hasOngoingEngagement) {
            chatRepository.sendMessage(payload, callback)
        } else {
            secureConversationsRepository.send(payload, queueIds, callback)
        }
    }

    private fun sendMessageWithAttachments(
        message: String,
        queueIds: List<String>,
        localAttachments: List<LocalAttachment>,
        callback: RequestCallback<VisitorMessage?>
    ) {
        val attachment = localAttachments
            .mapNotNull { it.engagementFile }
            .takeIf { it.isNotEmpty() }
            ?.run { FilesAttachment.from(toTypedArray()) }

        val payload = SendMessagePayload(content = message, attachment)

        if (hasOngoingEngagement) {
            chatRepository.sendMessage(payload, callback)
        } else {
            secureConversationsRepository.send(payload, queueIds, callback)
        }
    }
}
