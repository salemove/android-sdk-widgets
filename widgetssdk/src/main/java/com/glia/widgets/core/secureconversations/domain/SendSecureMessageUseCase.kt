package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.SendMessageRepository

class SendSecureMessageUseCase(
    private val queueId: String,
    private val sendMessageRepository: SendMessageRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val fileAttachmentRepository: SecureFileAttachmentRepository
) {

    operator fun invoke(
        callback: RequestCallback<VisitorMessage?>
    ) {
        val message = sendMessageRepository.value
        val queueIds = arrayOf(queueId)
        val fileAttachments = fileAttachmentRepository.getReadyToSendFileAttachments()
        if (fileAttachments.isNotEmpty()) {
            sendMessageWithAttachments(message, queueIds, fileAttachments, callback)
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

        secureConversationsRepository.send(message, queueIds, filesAttachment) { result, ex ->
            if (ex == null) {
                sendMessageRepository.reset()
                fileAttachmentRepository.detachFiles(fileAttachments)
            }
            callback.onResult(result, ex)
        }
    }
}
