package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.GvaGalleryCards
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal

internal class MapGvaGvaGalleryCardsUseCase(
    private val parseGvaGalleryCardsUseCase: ParseGvaGalleryCardsUseCase
) {
    operator fun invoke(chatMessage: ChatMessageInternal, showChatHead: Boolean): GvaGalleryCards {
        val message = chatMessage.chatMessage
        val metadata = message.metadata

        return GvaGalleryCards(
            id = message.id,
            galleryCards = parseGvaGalleryCardsUseCase(metadata),
            showChatHead = showChatHead,
            operatorId = chatMessage.operatorId,
            timestamp = message.timestamp,
            operatorProfileImgUrl = chatMessage.operatorImageUrl,
            operatorName = chatMessage.operatorName
        )
    }
}
