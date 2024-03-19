package com.glia.widgets.chat.domain

import com.glia.widgets.chat.data.ChatScreenRepository

internal class IsFromCallScreenUseCase(private val chatScreenRepository: ChatScreenRepository) {

    operator fun invoke(): Boolean = chatScreenRepository.isFromCallScreen()
}
