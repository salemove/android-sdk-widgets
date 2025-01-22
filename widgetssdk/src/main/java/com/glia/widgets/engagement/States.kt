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
    data class EngagementStarted(val type: EngagementType) : StateWithType(type)
    data class EngagementEnded(val type: EngagementType, val isEndedByVisitor: Boolean, val onEnd: ActionOnEnd) : StateWithType(type)
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

    open class StateWithType(engagementType: EngagementType): State {
        val isCallVisualizer = engagementType == EngagementType.CallVisualizer
    }
}

internal sealed interface EngagementUpdateState {
    data object Transferring : EngagementUpdateState
    data class Ongoing(val operator: Operator) : EngagementUpdateState
    data class OperatorConnected(val operator: Operator) : EngagementUpdateState
    data class OperatorChanged(val operator: Operator) : EngagementUpdateState
}

internal sealed interface SurveyState {
    data object Empty : SurveyState
    data class Value(val survey: Survey) : SurveyState
}

internal sealed interface ScreenSharingState {
    data object Requested : ScreenSharingState
    data object Started : ScreenSharingState
    data object RequestAccepted : ScreenSharingState
    data object RequestDeclined : ScreenSharingState
    data class FailedToAcceptRequest(val message: String) : ScreenSharingState
    data object Ended : ScreenSharingState
}

internal sealed interface EngagementType {
    data object OmniCore: EngagementType
    data object CallVisualizer: EngagementType
}
