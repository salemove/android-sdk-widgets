package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.secureconversations.SendMessageRepository

class OnNextMessageUseCase(private val sendMessageRepository: SendMessageRepository) {
    operator fun invoke(message: String) {
        sendMessageRepository.onNextMessage(message)
    }
}
