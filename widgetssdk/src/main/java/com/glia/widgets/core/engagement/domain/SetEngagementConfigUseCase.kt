package com.glia.widgets.core.engagement.domain

import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.di.Dependencies

internal class SetEngagementConfigUseCase(
    private val engagementConfigRepository: GliaEngagementConfigRepository
) {
    operator fun invoke(chatType: ChatType, queueIds: Array<String>) {
        engagementConfigRepository.chatType = chatType
        engagementConfigRepository.queueIds = queueIds

        // Resting just in case there is a pending Survey
        if (chatType == ChatType.SECURE_MESSAGING) {
            Dependencies.getRepositoryFactory().engagementRepository.reset()
        }
    }
}
