package com.glia.widgets.core.engagement.domain.model

internal data class ChatHistoryResponse(
    val items: List<ChatMessageInternal>,
    val newMessagesDividerIndex: Int = -1
)