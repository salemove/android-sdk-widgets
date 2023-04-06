package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.Chat
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal

class FindNewMessagesDividerIndexUseCase {

    operator fun invoke(messages: List<ChatMessageInternal>, unreadMessagesCount: Int): Int {
        if (messages.isEmpty() || unreadMessagesCount <= 0) return -1

        var dividerIndex = -1
        var remainingUnreadMessagesCount = unreadMessagesCount

        val iterator = messages.run { listIterator(size) }

        while (iterator.hasPrevious() && remainingUnreadMessagesCount > 0) {

            val message = iterator.previous()

            if (message.chatMessage.senderType != Chat.Participant.VISITOR) {
                --remainingUnreadMessagesCount
            }

            dividerIndex = iterator.nextIndex()
        }

        return dividerIndex.coerceAtLeast(0)
    }

}