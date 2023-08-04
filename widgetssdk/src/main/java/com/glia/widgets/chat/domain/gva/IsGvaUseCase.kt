package com.glia.widgets.chat.domain.gva

import com.glia.androidsdk.chat.ChatMessage

internal class IsGvaUseCase(
    private val getGvaTypeUseCase: GetGvaTypeUseCase
) {

    operator fun invoke(chatMessage: ChatMessage): Boolean = chatMessage.metadata?.let {
        getGvaTypeUseCase(it)
    } != null
}
