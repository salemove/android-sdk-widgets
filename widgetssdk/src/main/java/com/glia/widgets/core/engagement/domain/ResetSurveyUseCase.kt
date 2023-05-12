package com.glia.widgets.core.engagement.domain

import com.glia.widgets.core.chathead.SurveyStateManager
import com.glia.widgets.core.survey.GliaSurveyRepository

internal class ResetSurveyUseCase(
    private val surveyStateManager: SurveyStateManager,
    private val surveyRepository: GliaSurveyRepository
) {
    operator fun invoke() {
        surveyStateManager.preEngagement()
        surveyRepository.reset()
    }
}
