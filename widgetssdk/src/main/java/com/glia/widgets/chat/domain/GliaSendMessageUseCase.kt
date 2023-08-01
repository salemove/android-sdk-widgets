package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.engagement.GliaEngagementStateRepository
import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase

class GliaSendMessageUseCase(
    private val chatRepository: GliaChatRepository,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val engagementStateRepository: GliaEngagementStateRepository,
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val isSecureEngagementUseCase: IsSecureEngagementUseCase
) {
    interface Listener {
        fun messageSent(message: VisitorMessage?)
        fun onMessageValidated()
        fun errorOperatorNotOnline(message: String)
        fun error(ex: GliaException)

        fun errorMessageInvalid() {
            // Currently, no need for this method, but have to keep it because it describes case in else branch
        }
    }

    private val isSecureEngagement: Boolean
        get() = isSecureEngagementUseCase()

    private fun hasFileAttachments(fileAttachments: List<FileAttachment>): Boolean {
        return fileAttachments.isNotEmpty()
    }

    private fun sendMessageWithAttachments(
        message: String,
        fileAttachments: List<FileAttachment>,
        listener: Listener
    ) {
        val filesAttachment = fileAttachments
            .map { it.engagementFile }
            .toTypedArray()
            .let { FilesAttachment.from(it) }
        if (isSecureEngagement) {
            secureConversationsRepository.send(message, engagementConfigRepository.queueIds, filesAttachment, listener)
        } else {
            if (message.isNotEmpty()) {
                chatRepository.sendMessageWithAttachment(message, filesAttachment, listener)
            } else {
                chatRepository.sendMessageAttachment(filesAttachment, listener)
            }
        }
        fileAttachmentRepository.detachFiles(fileAttachments)
    }

    private fun sendMessage(message: String, listener: Listener) {
        if (isSecureEngagement) {
            secureConversationsRepository.send(message, engagementConfigRepository.queueIds, listener)
        } else {
            chatRepository.sendMessage(message, listener)
        }
    }

    fun execute(message: String, listener: Listener) {
        val fileAttachments: List<FileAttachment> =
            fileAttachmentRepository.readyToSendFileAttachments
        if (canSendMessage(message, fileAttachments.size)) {
            listener.onMessageValidated()
            if (isOperatorOnline || isSecureEngagement) {
                if (hasFileAttachments(fileAttachments)) {
                    sendMessageWithAttachments(message, fileAttachments, listener)
                } else {
                    sendMessage(message, listener)
                }
            } else {
                listener.errorOperatorNotOnline(message)
            }
        } else {
            listener.errorMessageInvalid()
        }
    }

    fun execute(singleChoiceAttachment: SingleChoiceAttachment?, listener: Listener?) {
        chatRepository.sendMessageSingleChoice(singleChoiceAttachment, listener)
    }

    private val isOperatorOnline: Boolean
        get() = engagementStateRepository.isOperatorPresent

    private fun canSendMessage(message: String, numOfAttachment: Int): Boolean {
        return message.isNotEmpty() || numOfAttachment > 0
    }
}
