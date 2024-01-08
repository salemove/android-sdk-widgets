package com.glia.widgets.engagement

import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.Operator
import com.glia.androidsdk.engagement.EngagementState
import com.glia.androidsdk.engagement.Survey

internal sealed interface State {
    object NoEngagement : State
    data class PreQueuing(val queueId: String, val mediaType: MediaType? = null) : State
    data class Queuing(val queueId: String, val queueTicketId: String, val mediaType: MediaType? = null) : State
    object QueueUnstaffed : State
    object UnexpectedErrorHappened : State
    object QueueingCanceled : State
    object StartedOmniCore : State
    object StartedCallVisualizer : State
    object FinishedOmniCore : State
    object FinishedCallVisualizer : State
    data class Update(val state: EngagementState, val updateState: EngagementUpdateState) : State
}

internal sealed interface EngagementUpdateState {
    object Transferring : EngagementUpdateState
    data class Ongoing(val operator: Operator) : EngagementUpdateState
    data class OperatorConnected(val operator: Operator) : EngagementUpdateState
    data class OperatorChanged(val operator: Operator) : EngagementUpdateState
}

internal sealed interface SurveyState {
    object Empty : SurveyState
    object EmptyFromOperatorRequest : SurveyState
    data class Value(val survey: Survey) : SurveyState
}

internal sealed interface ScreenSharingState {
    data class Requested(val operatorName: String) : ScreenSharingState
    object Started : ScreenSharingState
    object RequestAccepted : ScreenSharingState
    object RequestDeclined : ScreenSharingState
    data class FailedToAcceptRequest(val message: String) : ScreenSharingState
    object Ended : ScreenSharingState
}
