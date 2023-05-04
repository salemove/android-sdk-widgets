package com.glia.widgets.core.engagement.domain

import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.chathead.SurveyStateManager
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.survey.GliaSurveyRepository

class SetEngagementConfigUseCase(
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val surveyStateManager: SurveyStateManager,
    private val surveyRepository: GliaSurveyRepository
) {
    operator fun invoke(chatType: ChatType, queueIds: Array<String>) {
        engagementConfigRepository.chatType = chatType
        engagementConfigRepository.queueIds = queueIds

        // Reset state for secure messaging.
        // Just needed to show the chat transcript screen.
        if (chatType == ChatType.SECURE_MESSAGING) {
            surveyStateManager.preEngagement()
            surveyRepository.reset()
        }
    }
}