package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.engagement.GliaEngagementTypeRepository

class SetSecureEngagementUseCase(private val engagementTypeRepository: GliaEngagementTypeRepository) {
    operator fun invoke(isSecureEngagement: Boolean) {
        engagementTypeRepository.setIsSecureEngagement(isSecureEngagement)
    }
}