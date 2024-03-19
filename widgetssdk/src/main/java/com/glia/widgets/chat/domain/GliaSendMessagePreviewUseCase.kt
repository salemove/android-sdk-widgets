package com.glia.widgets.chat.domain

import com.glia.widgets.chat.data.GliaChatRepository

internal class GliaSendMessagePreviewUseCase(private val gliaChatRepository: GliaChatRepository) {
    operator fun invoke(message: String?) {
        gliaChatRepository.sendMessagePreview(message)
    }
}
