package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.engagement.GliaEngagementTypeRepository

class IsSecureEngagementUseCase(private val engagementTypeRepository: GliaEngagementTypeRepository) {
    operator fun invoke(): Boolean {
        return engagementTypeRepository.isSecureEngagement
    }
}