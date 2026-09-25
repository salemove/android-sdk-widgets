package com.glia.widgets.di

import com.glia.widgets.chat.ChatManager

internal class ManagerFactory(private val useCaseFactory: UseCaseFactory) {
    val chatManager: ChatManager
        get() = useCaseFactory.run {
            ChatManager(
                onMessageUseCase = createGliaOnMessageUseCase(),
                loadHistoryUseCase = createGliaLoadHistoryUseCase(),
                loadOlderHistoryUseCase = createLoadOlderHistoryUseCase(),
                hasOlderHistoryUseCase = createHasOlderHistoryUseCase(),
                addNewMessagesDividerUseCase = createAddNewMessagesDividerUseCase(),
                shouldMarkMessagesReadUseCase = createShouldMarkMessagesReadUseCase(),
                markMessagesReadWithDelayUseCase = createMarkMessagesReadUseCase(),
                appendHistoryChatMessageUseCase = createAppendHistoryChatMessageUseCase(),
                appendNewChatMessageUseCase = createAppendNewChatMessageUseCase(),
                sendUnsentMessagesUseCase = createSendUnsentMessagesUseCase(),
                handleCustomCardClickUseCase = createHandleCustomCardClickUseCase(),
                isAuthenticatedUseCase = createIsAuthenticatedUseCase(),
                isQueueingOrLiveEngagementUseCase = isQueueingOrEngagementUseCase
            )
        }
}
