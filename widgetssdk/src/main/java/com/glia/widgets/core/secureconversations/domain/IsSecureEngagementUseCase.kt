package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.engagement.GliaEngagementRepository

class IsSecureEngagementUseCase(private val engagementConfigRepository: GliaEngagementConfigRepository,
                                private val engagementRepository: GliaEngagementRepository
) {
    operator fun invoke(): Boolean {
        return engagementConfigRepository.chatType == ChatType.SECURE_MESSAGING
                && !engagementRepository.hasOngoingEngagement()
    }
}
