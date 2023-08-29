package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.core.engagement.GliaEngagementRepository
import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.SendMessageRepository

internal class SendSecureMessageUseCase(
    private val queueId: String,
    private val sendMessageRepository: SendMessageRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val fileAttachmentRepository: SecureFileAttachmentRepository,
    private val chatRepository: GliaChatRepository,
    private val engagementRepository: GliaEngagementRepository
) {

    private val hasOngoingEngagement: Boolean
        get() = engagementRepository.hasOngoingEngagement()

    operator fun invoke(
        callback: RequestCallback<VisitorMessage?>
    ) {
        val message = sendMessageRepository.value
        val queueIds = arrayOf(queueId)
        val fileAttachments = fileAttachmentRepository.getReadyToSendFileAttachments()
        sendMessage(message, queueIds, fileAttachments, callback)
    }

    private fun sendMessage(
        message: String,
        queueIds: Array<String>,
        fileAttachments: List<FileAttachment>,
        callback: RequestCallback<VisitorMessage?>
    ) {
        if (fileAttachments.isNotEmpty()) {
            sendMessageWithAttachments(message, queueIds, fileAttachments) { result, ex ->
                if (ex == null) {
                    sendMessageRepository.reset()
                    fileAttachmentRepository.detachFiles(fileAttachments)
                }
                callback.onResult(result, ex)
            }
        } else {
            sendMessage(message, queueIds, callback)
        }
    }

    private fun sendMessage(
        message: String,
        queueIds: Array<String>,
        callback: RequestCallback<VisitorMessage?>
    ) {
        if (hasOngoingEngagement) {
            chatRepository.sendMessage(message, callback)
        } else {
            secureConversationsRepository.send(message, queueIds, callback)
        }
    }

    private fun sendMessageWithAttachments(
        message: String,
        queueIds: Array<String>,
        fileAttachments: List<FileAttachment>,
        callback: RequestCallback<VisitorMessage?>
    ) {
        val filesAttachment = fileAttachments
            .map { it.engagementFile }
            .toTypedArray()
            .let { FilesAttachment.from(it) }

        if (hasOngoingEngagement) {
            chatRepository.sendMessageWithAttachment(message, filesAttachment, callback)
        } else {
            secureConversationsRepository.send(message, queueIds, filesAttachment, callback)
        }
    }
}
