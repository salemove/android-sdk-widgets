package com.glia.widgets.core.engagement.domain.model

import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.core.engagement.data.LocalOperator

internal data class ChatMessageInternal(val chatMessage: ChatMessage, val operator: LocalOperator? = null) {
    val operatorId: String? get() = operator?.id
    val operatorName: String? get() = operator?.name
    val operatorImageUrl: String? get() = operator?.imageUrl
    val isNotVisitor: Boolean get() = chatMessage.senderType != Chat.Participant.VISITOR
}
