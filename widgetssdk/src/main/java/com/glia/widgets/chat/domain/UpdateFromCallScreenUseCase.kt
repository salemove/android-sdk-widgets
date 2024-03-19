package com.glia.widgets.chat.domain

import com.glia.widgets.chat.data.ChatScreenRepository

internal class UpdateFromCallScreenUseCase(private val chatScreenRepository: ChatScreenRepository) {
    operator fun invoke(fromCallScreen: Boolean) {
        chatScreenRepository.setFromCallScreen(fromCallScreen)
    }
}
