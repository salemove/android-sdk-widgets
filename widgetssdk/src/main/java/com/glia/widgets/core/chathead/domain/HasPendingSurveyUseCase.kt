package com.glia.widgets.core.chathead.domain

import com.glia.widgets.core.chathead.SurveyStateManager
import com.glia.widgets.core.chathead.SurveyStep

class HasPendingSurveyUseCase(private val surveyStateManager: SurveyStateManager) {
    operator fun invoke() = surveyStateManager.getEngagementStep() == SurveyStep.SURVEY
}