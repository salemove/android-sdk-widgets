package com.glia.widgets.core.engagement.domain.model

import com.glia.widgets.core.secureconversations.domain.NO_UNREAD_MESSAGES

internal data class ChatHistoryResponse(
    val items: List<ChatMessageInternal>,
    val newMessagesCount: Int = NO_UNREAD_MESSAGES
)