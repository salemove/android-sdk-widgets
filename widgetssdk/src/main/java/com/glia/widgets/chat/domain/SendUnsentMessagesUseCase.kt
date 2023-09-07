package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.model.Unsent


internal class SendUnsentMessagesUseCase(private val chatRepository: GliaChatRepository) {
    operator fun invoke(message: Unsent, onSuccess: (VisitorMessage) -> Unit) {
        when (message) {
            is Unsent.Attachment -> sendAttachment(message.attachment, onSuccess)
            is Unsent.Message -> sendMessage(message.message, onSuccess)
        }
    }

    private fun sendAttachment(attachment: SingleChoiceAttachment, onSuccess: (VisitorMessage) -> Unit) {
        chatRepository.sendResponse(attachment) { response, exception ->
            if (exception == null && response != null) {
                onSuccess(response)
            }
        }
    }

    private fun sendMessage(message: String, onSuccess: (VisitorMessage) -> Unit) {
        chatRepository.sendMessage(message) { response, exception ->
            if (exception == null && response != null) {
                onSuccess(response)
            }
        }
    }
}
