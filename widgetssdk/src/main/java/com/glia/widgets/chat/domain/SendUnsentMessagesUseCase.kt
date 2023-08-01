package com.glia.widgets.chat.domain

import com.glia.widgets.chat.data.GliaChatRepository


internal class SendUnsentMessagesUseCase(private val chatRepository: GliaChatRepository) {
    operator fun invoke(message: String, onSuccess: () -> Unit) {
        chatRepository.sendMessage(message) { response, exception ->
            if (exception == null && response != null) {
                onSuccess()
            }
        }
    }
}
