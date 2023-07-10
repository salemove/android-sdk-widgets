package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.adapter.CustomCardAdapter

class CustomCardAdapterTypeUseCase(private val adapter: CustomCardAdapter?) {
    operator fun invoke(message: ChatMessage): Int? = when {
        adapter == null || message.metadata == null || message.metadata!!.length() == 0 -> null
        else -> adapter.getChatAdapterViewType(message)
    }
}
