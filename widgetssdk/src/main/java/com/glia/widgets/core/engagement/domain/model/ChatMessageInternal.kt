package com.glia.widgets.core.engagement.domain.model

import com.glia.androidsdk.Operator
import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import kotlin.jvm.optionals.getOrNull

internal data class ChatMessageInternal(
    val chatMessage: ChatMessage,
    val isHistory: Boolean = false,
    val isLatest: Boolean = false,
    val operator: Operator? = null
) {
    val operatorId: String? get() = operator?.id
    val operatorName: String? get() = operator?.name
    val operatorImageUrl: String? get() = operator?.picture?.url?.getOrNull()
    val isNotVisitor: Boolean get() = chatMessage.senderType != Chat.Participant.VISITOR
}
