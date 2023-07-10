package com.glia.widgets.chat.model.history

import com.glia.androidsdk.chat.SingleChoiceOption

data class ResponseCardItem(
    private val id: String,
    private val operatorName: String?,
    private val operatorProfileImgUrl: String?,
    private val showChatHead: Boolean,
    private val content: String?,
    private val operatorId: String?,
    private val timestamp: Long,
    val singleChoiceOptions: List<SingleChoiceOption>,
    val choiceCardImageUrl: String?
) : OperatorMessageItem(id, operatorName, operatorProfileImgUrl, showChatHead, content, operatorId, timestamp) {

    init {
        require(singleChoiceOptions.isNotEmpty()) { "Response card should have at least one `SingleChoiceOption`" }
    }
}
