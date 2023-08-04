package com.glia.widgets.chat.domain

import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.OperatorMessageItem

internal class IsEnableChatEditTextUseCase {

    // should enable only if there is no unselected choice-card last
    operator fun invoke(items: List<ChatItem>?): Boolean = items?.lastOrNull()?.let {
        it.viewType != ChatAdapter.OPERATOR_MESSAGE_VIEW_TYPE || it !is OperatorMessageItem.ResponseCard
    } ?: true
}
