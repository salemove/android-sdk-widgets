package com.glia.widgets.core.engagement.domain

import com.glia.widgets.core.engagement.GliaEngagementRepository

class IsOngoingEngagementUseCase(private val engagementRepository: GliaEngagementRepository) {
    operator fun invoke(): Boolean {
        return engagementRepository.hasOngoingEngagement()
    }
}