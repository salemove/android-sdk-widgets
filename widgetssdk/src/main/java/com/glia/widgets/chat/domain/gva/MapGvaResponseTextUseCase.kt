package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaResponseText
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import java.util.UUID

internal class MapGvaResponseTextUseCase {
    operator fun invoke(chatMessage: ChatMessageInternal, chatState: ChatState): GvaResponseText {
        val message = chatMessage.chatMessage

        return GvaResponseText(
            id = message.id,
            content = message.metadata?.optString(Gva.Keys.CONTENT).orEmpty(),
            showChatHead = false,
            operatorId = chatMessage.operatorId ?: UUID.randomUUID().toString(),
            timestamp = message.timestamp,
            operatorProfileImgUrl = chatMessage.operatorImageUrl ?: chatState.operatorProfileImgUrl,
            operatorName = chatMessage.operatorName ?: chatState.formattedOperatorName
        )
    }
}
