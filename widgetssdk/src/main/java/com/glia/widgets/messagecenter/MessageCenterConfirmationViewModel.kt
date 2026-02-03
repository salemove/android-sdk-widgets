package com.glia.widgets.messagecenter

import com.glia.widgets.base.BaseViewModel
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.internal.secureconversations.domain.ResetMessageCenterUseCase

internal class MessageCenterConfirmationViewModel(
    private val resetMessageCenterUseCase: ResetMessageCenterUseCase,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
) : BaseViewModel<MessageCenterConfirmationUiState, MessageCenterConfirmationIntent, MessageCenterConfirmationEffect>(
    MessageCenterConfirmationUiState()
) {

    override suspend fun handleIntent(intent: MessageCenterConfirmationIntent) {
        when (intent) {
            MessageCenterConfirmationIntent.CheckMessagesClicked -> handleCheckMessages()
            MessageCenterConfirmationIntent.CloseClicked -> handleClose()
        }
    }

    private fun handleCheckMessages() {
        if (isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) {
            sendEffect(MessageCenterConfirmationEffect.ReturnToLiveChat)
        } else {
            sendEffect(MessageCenterConfirmationEffect.NavigateToMessaging)
        }
        reset()
    }

    private fun handleClose() {
        sendEffect(MessageCenterConfirmationEffect.Finish)
        reset()
    }

    private fun reset() {
        resetMessageCenterUseCase()
    }
}