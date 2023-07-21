package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaPersistentButtons
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import java.util.UUID

internal class MapGvaPersistentButtonsUseCase(
    private val parseGvaButtonsUseCase: ParseGvaButtonsUseCase
) {
    operator fun invoke(chatMessage: ChatMessageInternal, chatState: ChatState): GvaPersistentButtons {
        val message = chatMessage.chatMessage
        val metadata = message.metadata

        return GvaPersistentButtons(
            id = message.id,
            content = metadata?.optString(Gva.Keys.CONTENT).orEmpty(),
            options = parseGvaButtonsUseCase(metadata),
            showChatHead = false,
            operatorId = chatMessage.operatorId ?: UUID.randomUUID().toString(),
            timestamp = message.timestamp,
            operatorProfileImgUrl = chatMessage.operatorImageUrl ?: chatState.operatorProfileImgUrl,
            operatorName = chatMessage.operatorName ?: chatState.formattedOperatorName
        )
    }
}
