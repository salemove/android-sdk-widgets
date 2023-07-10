package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.history.GvaChatItem
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal

internal class MapGvaUseCase(
    private val getGvaTypeUseCase: GetGvaTypeUseCase,
    private val mapGvaResponseTextUseCase: MapGvaResponseTextUseCase,
    private val mapGvaPersistentButtonsUseCase: MapGvaPersistentButtonsUseCase,
    private val mapGvaGvaQuickRepliesUseCase: MapGvaGvaQuickRepliesUseCase,
    private val mapGvaGvaGalleryCardsUseCase: MapGvaGvaGalleryCardsUseCase
) {
    operator fun invoke(chatMessageInternal: ChatMessageInternal, chatState: ChatState): GvaChatItem =
        when (getGvaTypeUseCase(chatMessageInternal.chatMessage.metadata!!)) {
            Gva.Type.PLAIN_TEXT -> mapGvaResponseTextUseCase(chatMessageInternal, chatState)
            Gva.Type.PERSISTENT_BUTTONS -> mapGvaPersistentButtonsUseCase(chatMessageInternal, chatState)
            Gva.Type.QUICK_REPLIES -> mapGvaGvaQuickRepliesUseCase(chatMessageInternal, chatState)
            Gva.Type.GALLERY_CARDS -> mapGvaGvaGalleryCardsUseCase(chatMessageInternal, chatState)
            else -> throw IllegalArgumentException("metadata should contain on of the [${Gva.Type.values().joinToString { it.value }}] types")
        }
}
