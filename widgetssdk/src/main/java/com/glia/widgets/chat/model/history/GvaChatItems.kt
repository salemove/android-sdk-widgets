package com.glia.widgets.chat.model.history

import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.GvaGalleryCard

sealed interface GvaChatItem

internal data class GvaResponseText(
    private val id: String = "",
    val content: String = "",
    private val showChatHead: Boolean = false,
    private val operatorId: String = "",
    val timeStamp: Long = 0L,
    val operatorProfileImageUrl: String? = null,
    val operatorName: String? = null
) : GvaChatItem,
    OperatorChatItem(id, ChatAdapter.GVA_RESPONSE_TEXT_TYPE, showChatHead, operatorProfileImageUrl, operatorId, id, timeStamp)

internal data class GvaPersistentButtons(
    private val id: String = "",
    val content: String = "",
    val options: List<GvaButton> = listOf(),
    private val showChatHead: Boolean = false,
    private val operatorId: String = "",
    val timeStamp: Long = 0L,
    val operatorProfileImageUrl: String? = null,
    val operatorName: String? = null
) : GvaChatItem,
    OperatorChatItem(id, ChatAdapter.GVA_PERSISTENT_BUTTONS_TYPE, showChatHead, operatorProfileImageUrl, operatorId, id, timeStamp)

internal data class GvaGalleryCards(
    private val id: String = "",
    val galleryCards: List<GvaGalleryCard>,
    private val showChatHead: Boolean = false,
    private val operatorId: String = "",
    val timeStamp: Long = 0L,
    val operatorProfileImageUrl: String? = null,
    val operatorName: String? = null
) : GvaChatItem,
    OperatorChatItem(id, ChatAdapter.GVA_GALLERY_CARDS_TYPE, showChatHead, operatorProfileImageUrl, operatorId, id, timeStamp)

internal data class GvaQuickReplies(
    val gvaResponseText: GvaResponseText,
    val options: List<GvaButton> = listOf(),
) : GvaChatItem
