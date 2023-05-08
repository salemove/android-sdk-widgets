package com.glia.widgets.core.chathead

class SurveyStateManager {
    private var surveyStep: SurveyStep = SurveyStep.PRE_ENGAGEMENT

    fun getEngagementStep(): SurveyStep {
        return surveyStep
    }

    fun pendingSurvey() {
        surveyStep = SurveyStep.SURVEY
    }

    fun engagementEnded() {
        surveyStep = SurveyStep.ENDED
    }

    fun preEngagement() {
        surveyStep = SurveyStep.PRE_ENGAGEMENT
    }
}

enum class SurveyStep {
    PRE_ENGAGEMENT,
    SURVEY,
    ENDED
}
