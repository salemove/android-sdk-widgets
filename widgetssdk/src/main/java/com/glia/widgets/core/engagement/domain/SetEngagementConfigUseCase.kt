package com.glia.widgets.core.engagement.domain

import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository

internal class SetEngagementConfigUseCase(
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val resetSurveyUseCase: ResetSurveyUseCase
) {
    operator fun invoke(chatType: ChatType, queueIds: Array<String>) {
        engagementConfigRepository.chatType = chatType
        engagementConfigRepository.queueIds = queueIds

        // Reset state for secure messaging.
        // Just needed to show the chat transcript screen.
        if (chatType == ChatType.SECURE_MESSAGING) {
            resetSurveyUseCase()
        }
    }
}
