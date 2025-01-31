package com.glia.widgets.engagement.completion

import com.glia.androidsdk.engagement.Survey

internal sealed interface EngagementCompletionState {
    data object ShowNoOperatorsAvailableDialog : EngagementCompletionState
    data object ShowUnexpectedErrorDialog : EngagementCompletionState
    data object FinishActivities : EngagementCompletionState
    data object ShowEngagementEndedDialog : EngagementCompletionState
    data class ShowSurvey(val survey: Survey) : EngagementCompletionState
}
