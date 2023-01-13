package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.secureconversations.SecureConversationsRepository

class SendSecureMessageUseCase(
    private val queueId: String,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val fileAttachmentRepository: SecureFileAttachmentRepository
) {

    fun execute(
        message: String,
        callback: RequestCallback<VisitorMessage?>
    ) {
        val queueIds = arrayOf(queueId)
        val fileAttachments = fileAttachmentRepository.getReadyToSendFileAttachments()
        if (fileAttachments != null && fileAttachments.isNotEmpty()) {
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

        secureConversationsRepository.send(message, queueIds, filesAttachment, callback)
        fileAttachmentRepository.detachFiles(fileAttachments)
    }
}
