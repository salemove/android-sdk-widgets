package com.glia.widgets.engagement

import com.glia.androidsdk.Operator
import com.glia.androidsdk.engagement.EngagementState
import com.glia.androidsdk.engagement.Survey

internal sealed interface State {
    data object NoEngagement : State
    data class PreQueuing(val mediaType: MediaType) : State
    data class Queuing(val queueTicketId: String, val mediaType: MediaType) : State
    data object QueueUnstaffed : State
    data object UnexpectedErrorHappened : State
    data object QueueingCanceled : State
    data class EngagementStarted(val isCallVisualizer: Boolean) : State
    data class EngagementEnded(val endAction: EndAction) : State

    data object TransferredToSecureConversation : State
    data class Update(val state: EngagementState, val updateState: EngagementUpdateState) : State

    val isQueueing: Boolean
        get() = this is Queuing || this is PreQueuing

    val isLiveEngagement: Boolean
        get() = when (this) {
            is Update, is EngagementStarted -> true
            else -> false
        }

    val queueingMediaType: MediaType?
        get() = when (this) {
            is PreQueuing -> mediaType
            is Queuing -> mediaType
            else -> null
        }
}

internal sealed interface EndAction {
    data object Retain : EndAction
    data object ClearStateRegular : EndAction
    data object ClearStateCallVisualizer : EndAction
    data class ShowSurvey(val survey: Survey) : EndAction
    data object ShowEndDialog : EndAction
}

internal sealed interface EngagementUpdateState {
    data object Transferring : EngagementUpdateState
    data class Ongoing(val operator: Operator) : EngagementUpdateState
    data class OperatorConnected(val operator: Operator) : EngagementUpdateState
    data class OperatorChanged(val operator: Operator) : EngagementUpdateState
}

internal sealed interface ScreenSharingState {
    data object Requested : ScreenSharingState
    data object Started : ScreenSharingState
    data object RequestAccepted : ScreenSharingState
    data object RequestDeclined : ScreenSharingState
    data class FailedToAcceptRequest(val message: String) : ScreenSharingState
    data object Ended : ScreenSharingState
}
