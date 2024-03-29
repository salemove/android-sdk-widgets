package com.glia.widgets.chat.model

import com.glia.widgets.chat.adapter.ChatAdapter

internal abstract class GvaOperatorChatItem(@ChatAdapter.Type viewType: Int) : OperatorChatItem(viewType) {
    abstract val operatorName: String?
}

internal data class GvaResponseText(
    override val id: String = "",
    val content: String = "",
    override val showChatHead: Boolean = false,
    override val operatorId: String? = "",
    override val timestamp: Long = -1,
    override val operatorProfileImgUrl: String? = null,
    override val operatorName: String? = null
) : GvaOperatorChatItem(ChatAdapter.GVA_RESPONSE_TEXT_TYPE) {
    override fun withShowChatHead(showChatHead: Boolean): OperatorChatItem = copy(showChatHead = showChatHead)
}

internal data class GvaPersistentButtons(
    override val id: String = "",
    val content: String = "",
    val options: List<GvaButton> = listOf(),
    override val showChatHead: Boolean = false,
    override val operatorId: String? = "",
    override val timestamp: Long = -1,
    override val operatorProfileImgUrl: String? = null,
    override val operatorName: String? = null
) : GvaOperatorChatItem(ChatAdapter.GVA_PERSISTENT_BUTTONS_TYPE) {
    override fun withShowChatHead(showChatHead: Boolean): OperatorChatItem = copy(showChatHead = showChatHead)
}

internal data class GvaGalleryCards(
    override val id: String = "",
    val galleryCards: List<GvaGalleryCard>,
    override val showChatHead: Boolean = false,
    override val operatorId: String? = "",
    override val timestamp: Long = -1,
    override val operatorProfileImgUrl: String? = null,
    override val operatorName: String? = null
) : GvaOperatorChatItem(ChatAdapter.GVA_GALLERY_CARDS_TYPE) {
    override fun withShowChatHead(showChatHead: Boolean): OperatorChatItem = copy(showChatHead = showChatHead)
}

internal data class GvaQuickReplies(
    override val id: String = "",
    val content: String = "",
    override val showChatHead: Boolean = false,
    override val operatorId: String? = "",
    override val timestamp: Long = -1,
    override val operatorProfileImgUrl: String? = null,
    override val operatorName: String? = null,
    val options: List<GvaButton> = listOf()
) : GvaOperatorChatItem(ChatAdapter.GVA_QUICK_REPLIES_TYPE) {
    override fun withShowChatHead(showChatHead: Boolean): OperatorChatItem = copy(showChatHead = showChatHead)

    fun asResponseText(): GvaResponseText = GvaResponseText(
        id,
        content,
        showChatHead,
        operatorId,
        timestamp,
        operatorProfileImgUrl,
        operatorName
    )
}
