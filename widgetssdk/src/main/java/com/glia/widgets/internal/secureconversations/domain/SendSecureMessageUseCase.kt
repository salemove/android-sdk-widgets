package com.glia.widgets.internal.secureconversations.domain

import com.glia.androidsdk.GliaException
import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.internal.secureconversations.SendMessageRepository
import java.util.UUID

/**
 * Use case for sending the Message Center message as a secure conversation.
 *
 * Sends the current [SendMessageRepository] value together with any ready-to-send file
 * attachments through [SecureConversationsRepository]. On a successful send with attachments,
 * the composed message and the attachments are cleared before `onSuccess` is invoked.
 */
internal class SendSecureMessageUseCase(
    private val sendMessageRepository: SendMessageRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val fileAttachmentRepository: FileAttachmentRepository
) {

    operator fun invoke(onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
        val message = sendMessageRepository.value
        val fileAttachments = fileAttachmentRepository.getReadyToSendFileAttachments()
        sendMessage(message, fileAttachments, onSuccess, onFailure)
    }

    private fun sendMessage(message: String, localAttachments: List<LocalAttachment>, onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
        if (localAttachments.isNotEmpty()) {
            sendMessageWithAttachments(message, localAttachments, {
                sendMessageRepository.reset()
                fileAttachmentRepository.detachFiles(localAttachments)
                onSuccess()
            }, onFailure)
        } else {
            secureConversationsRepository.sendMessage(
                content = message,
                messageId = UUID.randomUUID().toString(),
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        }
    }

    private fun sendMessageWithAttachments(
        message: String,
        localAttachments: List<LocalAttachment>,
        onSuccess: () -> Unit,
        onFailure: (GliaException) -> Unit
    ) {
        val attachments = localAttachments
            .mapNotNull { it.engagementFile?.id }

        secureConversationsRepository.sendMessageWithAttachments(
            content = message,
            fileAttachments = attachments,
            messageId = UUID.randomUUID().toString(),
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}
