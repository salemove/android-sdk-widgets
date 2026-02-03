package com.glia.widgets.messagecenter

import com.glia.widgets.base.UiEffect
import com.glia.widgets.base.UiIntent
import com.glia.widgets.base.UiState

/**
 * UI state for MessageCenter confirmation screen.
 */
internal data class MessageCenterConfirmationUiState(
    val dummy: Boolean = true // Placeholder - confirmation screen has no dynamic state
) : UiState

/**
 * User intents for MessageCenter confirmation screen.
 */
internal sealed interface MessageCenterConfirmationIntent : UiIntent {
    data object CheckMessagesClicked : MessageCenterConfirmationIntent
    data object CloseClicked : MessageCenterConfirmationIntent
}

/**
 * One-time effects for MessageCenter confirmation screen.
 */
internal sealed interface MessageCenterConfirmationEffect : UiEffect {
    data object NavigateToMessaging : MessageCenterConfirmationEffect
    data object ReturnToLiveChat : MessageCenterConfirmationEffect
    data object Finish : MessageCenterConfirmationEffect
}