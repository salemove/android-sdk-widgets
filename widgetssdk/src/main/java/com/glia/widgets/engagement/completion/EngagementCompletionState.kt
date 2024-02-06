package com.glia.widgets.engagement.completion

import com.glia.androidsdk.engagement.Survey

sealed interface EngagementCompletionState {
    object QueueUnstaffed : EngagementCompletionState
    object UnexpectedErrorHappened : EngagementCompletionState
    object OperatorEndedEngagement : EngagementCompletionState
    object QueuingOrEngagementEnded : EngagementCompletionState
    data class SurveyLoaded(val survey: Survey) : EngagementCompletionState
}
