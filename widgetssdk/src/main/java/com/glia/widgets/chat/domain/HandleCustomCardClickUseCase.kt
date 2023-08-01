package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.model.CustomCardChatItem

internal class HandleCustomCardClickUseCase(
    private val customCardTypeUseCase: CustomCardTypeUseCase,
    private val customCardShouldShowUseCase: CustomCardShouldShowUseCase
) {
    operator fun invoke(
        customCard: CustomCardChatItem,
        attachment: SingleChoiceAttachment,
        state: ChatManager.State
    ): ChatManager.State {
        val customCardType = customCardTypeUseCase.execute(customCard.viewType) ?: return state
        val currentMessage = customCard.message
        val showCustomCard = customCardShouldShowUseCase.execute(
            currentMessage,
            customCardType,
            false
        )
        if (!showCustomCard) {
            state.chatItems.remove(customCard)
        } else {
            val index = state.chatItems.indexOf(customCard)
            val message = (currentMessage as? OperatorMessage)
            if (index != -1 && message != null) {
                val newMessage = message.let {
                    ChatMessage(
                        it.id,
                        it.content,
                        it.timestamp,
                        ChatMessage.Sender(it.senderType, it.operatorHref, it.operatorId),
                        it.deliveredAt,
                        attachment,
                        it.metadata
                    )
                }

                state.chatItems[index] = CustomCardChatItem(newMessage, customCard.viewType)
            }
        }
        return state
    }
}
