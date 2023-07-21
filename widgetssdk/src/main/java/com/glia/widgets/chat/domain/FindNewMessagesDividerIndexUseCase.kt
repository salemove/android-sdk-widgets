package com.glia.widgets.chat.domain

import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.ServerChatItem

internal const val NOT_PROVIDED = -1

internal class FindNewMessagesDividerIndexUseCase {

    /**
     * Calculates the `new messages divider` index.
     * @return the index between [NOT_PROVIDED] and [messages] count.
     */
    operator fun invoke(messages: List<ChatItem>, unreadMessagesCount: Int): Int {
        if (unreadMessagesCount !in (1..messages.count())) return NOT_PROVIDED

        var dividerIndex = NOT_PROVIDED
        var remainingUnreadMessagesCount = unreadMessagesCount

        val iterator = messages.run { listIterator(size) }

        while (iterator.hasPrevious() && remainingUnreadMessagesCount > 0) {
            val message = iterator.previous()

            if (message is ServerChatItem) {
                --remainingUnreadMessagesCount
            }

            dividerIndex = iterator.nextIndex()
        }

        return dividerIndex
    }
}
