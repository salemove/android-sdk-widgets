package com.glia.widgets.chat.domain

import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.NewMessagesDividerItem

internal class AddNewMessagesDividerUseCase(
    private val findNewMessagesDividerIndexUseCase: FindNewMessagesDividerIndexUseCase
) {
    operator fun invoke(messages: MutableList<ChatItem>, unreadMessagesCount: Int): Boolean {
        val index = findNewMessagesDividerIndexUseCase(messages, unreadMessagesCount)

        if (index != NOT_PROVIDED) {
            messages.add(index, NewMessagesDividerItem)
            return true
        }

        return false
    }
}
