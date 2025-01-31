package com.glia.widgets.engagement

import com.glia.androidsdk.Engagement.ActionOnEnd
import com.glia.androidsdk.Engagement.MediaType
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
    data class EngagementEnded(
        val isCallVisualizer: Boolean,
        val endedBy: EndedBy,
        val action: ActionOnEnd,
        val fetchSurveyCallback: FetchSurveyCallback
    ) : State

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

internal enum class EndedBy {
    CLEAR_STATE,
    OPERATOR,
    VISITOR
}

internal typealias FetchSurveyCallback = (onSuccess: (Survey) -> Unit, onError: () -> Unit) -> Unit

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
