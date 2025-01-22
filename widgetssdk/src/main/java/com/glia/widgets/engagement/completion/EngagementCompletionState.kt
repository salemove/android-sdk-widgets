package com.glia.widgets.engagement.completion

import com.glia.androidsdk.Engagement.ActionOnEnd
import com.glia.androidsdk.engagement.Survey

internal sealed interface EngagementCompletionState {
    data object QueueUnstaffed : EngagementCompletionState
    data object UnexpectedErrorHappened : EngagementCompletionState
    data class EngagementEnded(val isEndedByVisitor: Boolean, val actionOnEnd: ActionOnEnd) : EngagementCompletionState
    data class SurveyLoaded(val survey: Survey) : EngagementCompletionState
}
