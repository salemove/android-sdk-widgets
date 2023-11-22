package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.model.SendMessagePayload
import com.glia.widgets.chat.model.Unsent
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase
import com.glia.widgets.engagement.IsOperatorPresentUseCase

internal class GliaSendMessageUseCase(
    private val chatRepository: GliaChatRepository,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val isOperatorPresentUseCase: IsOperatorPresentUseCase,
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val isSecureEngagementUseCase: IsSecureEngagementUseCase
) {
    interface Listener {
        fun messageSent(message: VisitorMessage?)
        fun onMessageValidated()
        fun errorOperatorNotOnline(message: Unsent)
        fun error(ex: GliaException, message: Unsent)

        fun errorMessageInvalid() {
            // Currently, no need for this method, but have to keep it because it describes case in else branch
        }
    }

    private val isSecureEngagement: Boolean
        get() = isSecureEngagementUseCase()

    private fun hasFileAttachments(fileAttachments: List<FileAttachment>): Boolean {
        return fileAttachments.isNotEmpty()
    }

    private fun sendMessage(payload: SendMessagePayload, listener: Listener) {
        if (isSecureEngagement) {
            secureConversationsRepository.send(payload, engagementConfigRepository.queueIds, listener)
        } else {
            chatRepository.sendMessage(payload, listener)
        }
    }

    fun execute(message: String, listener: Listener) {
        val fileAttachments: List<FileAttachment> =
            fileAttachmentRepository.readyToSendFileAttachments
        if (canSendMessage(message, fileAttachments.size)) {
            listener.onMessageValidated()
            val attachments = if (hasFileAttachments(fileAttachments)) fileAttachments else null
            val payload = SendMessagePayload(content = message, fileAttachments = attachments)
            if (isOperatorOnline || isSecureEngagement) {
                sendMessage(payload, listener)
            } else {
                listener.errorOperatorNotOnline(Unsent(payload = payload))
            }
            fileAttachmentRepository.detachFiles(fileAttachments)
        } else {
            listener.errorMessageInvalid()
        }
    }

    fun execute(singleChoiceAttachment: SingleChoiceAttachment, listener: Listener) {
        val payload = SendMessagePayload(attachment = singleChoiceAttachment)
        when {
            isSecureEngagement -> secureConversationsRepository.send(payload, engagementConfigRepository.queueIds, listener)

            isOperatorOnline -> chatRepository.sendMessage(payload, listener)

            else -> listener.errorOperatorNotOnline(Unsent(payload = payload))
        }
    }

    private val isOperatorOnline: Boolean
        get() = isOperatorPresentUseCase()

    private fun canSendMessage(message: String, numOfAttachment: Int): Boolean {
        return message.isNotEmpty() || numOfAttachment > 0
    }
}
