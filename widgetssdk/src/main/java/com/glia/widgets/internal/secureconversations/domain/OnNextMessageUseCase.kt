package com.glia.widgets.internal.secureconversations.domain

import com.glia.widgets.internal.secureconversations.SendMessageRepository

internal class OnNextMessageUseCase(private val sendMessageRepository: SendMessageRepository) {
    operator fun invoke(message: String) {
        sendMessageRepository.onNextMessage(message)
    }
}
