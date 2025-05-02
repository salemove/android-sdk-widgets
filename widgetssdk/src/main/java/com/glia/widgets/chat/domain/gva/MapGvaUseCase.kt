package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.OperatorChatItem
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal

internal class MapGvaUseCase(
    private val getGvaTypeUseCase: GetGvaTypeUseCase,
    private val mapGvaResponseTextUseCase: MapGvaResponseTextUseCase,
    private val mapGvaPersistentButtonsUseCase: MapGvaPersistentButtonsUseCase,
    private val mapGvaQuickRepliesUseCase: MapGvaQuickRepliesUseCase,
    private val mapGvaGvaGalleryCardsUseCase: MapGvaGvaGalleryCardsUseCase
) {
    operator fun invoke(chatMessageInternal: ChatMessageInternal, showChatHead: Boolean = true): OperatorChatItem =
        when (getGvaTypeUseCase(chatMessageInternal.chatMessage.metadata!!)) {
            Gva.Type.PLAIN_TEXT -> mapGvaResponseTextUseCase(chatMessageInternal, showChatHead)
            Gva.Type.PERSISTENT_BUTTONS -> mapGvaPersistentButtonsUseCase(chatMessageInternal, showChatHead)
            Gva.Type.QUICK_REPLIES -> mapGvaQuickRepliesUseCase(chatMessageInternal, showChatHead)
            Gva.Type.GALLERY_CARDS -> mapGvaGvaGalleryCardsUseCase(chatMessageInternal, showChatHead)
            else -> throw IllegalArgumentException("metadata should contain on of the [${Gva.Type.values().joinToString { it.value }}] types")
        }
}
