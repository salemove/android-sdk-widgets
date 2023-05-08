package com.glia.widgets.chat.domain

import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.model.history.ChatItem
import com.glia.widgets.chat.model.history.ResponseCardItem

class IsEnableChatEditTextUseCase {

    // should enable only if there is no unselected choice-card last
    operator fun invoke(items: List<ChatItem>?): Boolean = items?.lastOrNull()?.let {
        it.viewType != ChatAdapter.OPERATOR_MESSAGE_VIEW_TYPE || it !is ResponseCardItem
    } ?: true
}
