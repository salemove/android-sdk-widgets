package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.history.GvaChatItem
import com.glia.widgets.chat.model.history.GvaQuickReplies
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal

internal class MapGvaGvaQuickRepliesUseCase(
    private val parseGvaButtonsUseCase: ParseGvaButtonsUseCase,
    private val mapGvaResponseTextUseCase: MapGvaResponseTextUseCase
) {
    operator fun invoke(chatMessage: ChatMessageInternal, chatState: ChatState): GvaChatItem {
        val message = chatMessage.chatMessage
        val metadata = message.metadata

        if (chatMessage.isHistory && !chatMessage.isLatest) {
            return mapGvaResponseTextUseCase(chatMessage, chatState)
        }

        return GvaQuickReplies(
            gvaResponseText = mapGvaResponseTextUseCase(chatMessage, chatState),
            options = parseGvaButtonsUseCase(metadata),
        )
    }
}
