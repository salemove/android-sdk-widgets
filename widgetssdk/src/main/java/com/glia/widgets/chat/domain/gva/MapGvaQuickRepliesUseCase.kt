package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaQuickReplies
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal

internal class MapGvaQuickRepliesUseCase(private val parseGvaButtonsUseCase: ParseGvaButtonsUseCase) {
    operator fun invoke(message: ChatMessageInternal, showChatHead: Boolean): GvaQuickReplies =
        message.run {
            GvaQuickReplies(
                id = chatMessage.id,
                content = chatMessage.metadata?.optString(Gva.Keys.CONTENT).orEmpty(),
                showChatHead = showChatHead,
                operatorId = operatorId,
                timestamp = chatMessage.timestamp,
                operatorProfileImgUrl = operatorImageUrl,
                operatorName = operatorName,
                options = parseGvaButtonsUseCase(chatMessage.metadata)
            )
        }
}
