package com.glia.widgets.internal.engagement.domain.model

internal data class ChatHistoryResponse(val items: List<ChatMessageInternal>, val newMessagesCount: Int = 0)
