package com.glia.widgets.core.chathead.domain

import com.glia.widgets.core.chathead.SurveyStateManager

class SetPendingSurveyUseCase(private val surveyStateManager: SurveyStateManager) {
    operator fun invoke() = surveyStateManager.pendingSurvey()
}