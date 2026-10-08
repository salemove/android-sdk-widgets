package com.glia.widgets.internal.engagement.domain.model

import com.glia.androidsdk.chat.ChatHistory

/** @property olderPage key of the page before [items]; `null` when there is no older history. */
internal data class ChatHistoryResponse(
    val items: List<ChatMessageInternal>,
    val newMessagesCount: Int = 0,
    val olderPage: ChatHistory.OlderPage? = null
)
