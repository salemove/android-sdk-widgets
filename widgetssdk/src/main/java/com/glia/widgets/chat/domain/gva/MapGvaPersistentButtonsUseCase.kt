package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaPersistentButtons
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal

internal class MapGvaPersistentButtonsUseCase(
    private val parseGvaButtonsUseCase: ParseGvaButtonsUseCase
) {
    operator fun invoke(chatMessage: ChatMessageInternal, showChatHead: Boolean): GvaPersistentButtons {
        val message = chatMessage.chatMessage
        val metadata = message.metadata

        return GvaPersistentButtons(
            id = message.id,
            content = metadata?.optString(Gva.Keys.CONTENT).orEmpty(),
            options = parseGvaButtonsUseCase(metadata),
            showChatHead = showChatHead,
            operatorId = chatMessage.operatorId,
            timestamp = message.timestamp,
            operatorProfileImgUrl = chatMessage.operatorImageUrl,
            operatorName = chatMessage.operatorName
        )
    }
}
