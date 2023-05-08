package com.glia.widgets.core.chathead.domain

import com.glia.widgets.core.chathead.SurveyStateManager

class SetPendingSurveyUsedUseCase(private val surveyStateManager: SurveyStateManager) {
    operator fun invoke() = surveyStateManager.engagementEnded()
}
