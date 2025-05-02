package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaResponseText
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal

internal class MapGvaResponseTextUseCase {
    operator fun invoke(chatMessage: ChatMessageInternal, showChatHead: Boolean): GvaResponseText {
        val message = chatMessage.chatMessage

        return GvaResponseText(
            id = message.id,
            content = message.metadata?.optString(Gva.Keys.CONTENT).orEmpty(),
            showChatHead = showChatHead,
            operatorId = chatMessage.operatorId,
            timestamp = message.timestamp,
            operatorProfileImgUrl = chatMessage.operatorImageUrl,
            operatorName = chatMessage.operatorName
        )
    }
}
