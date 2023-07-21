package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.GvaGalleryCards
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import java.util.UUID

internal class MapGvaGvaGalleryCardsUseCase(
    private val parseGvaGalleryCardsUseCase: ParseGvaGalleryCardsUseCase
) {
    operator fun invoke(chatMessage: ChatMessageInternal, chatState: ChatState): GvaGalleryCards {
        val message = chatMessage.chatMessage
        val metadata = message.metadata

        return GvaGalleryCards(
            id = message.id,
            galleryCards = parseGvaGalleryCardsUseCase(metadata),
            showChatHead = false,
            operatorId = chatMessage.operatorId ?: UUID.randomUUID().toString(),
            timestamp = message.timestamp,
            operatorProfileImgUrl = chatMessage.operatorImageUrl ?: chatState.operatorProfileImgUrl,
            operatorName = chatMessage.operatorName ?: chatState.formattedOperatorName
        )
    }
}
