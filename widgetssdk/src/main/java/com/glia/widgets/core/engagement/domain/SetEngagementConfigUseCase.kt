package com.glia.widgets.core.engagement.domain

import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.engagement.EngagementRepository

internal class SetEngagementConfigUseCase(
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val engagementRepository: EngagementRepository
) {
    operator fun invoke(chatType: ChatType, queueIds: Array<String>) {
        engagementConfigRepository.chatType = chatType
        engagementConfigRepository.queueIds = queueIds

        // Resetting just in case there is a pending Survey
        if (chatType == ChatType.SECURE_MESSAGING) {
            engagementRepository.reset()
        }
    }
}
