package com.glia.widgets.chat.domain

import com.glia.widgets.chat.data.ChatScreenRepository

internal class SetChatScreenOpenUseCase(private val chatScreenRepository: ChatScreenRepository) {
    operator fun invoke(chatScreenOpen: Boolean) {
        chatScreenRepository.setChatScreenOpen(chatScreenOpen)
    }
}
