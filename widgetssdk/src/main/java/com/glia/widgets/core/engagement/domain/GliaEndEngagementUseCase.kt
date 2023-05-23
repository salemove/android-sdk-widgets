package com.glia.widgets.core.engagement.domain

import com.glia.widgets.chat.data.ChatScreenRepository
import com.glia.widgets.core.engagement.GliaEngagementRepository

class GliaEndEngagementUseCase(
    private val engagementRepository: GliaEngagementRepository,
    private val chatScreenRepository: ChatScreenRepository
) {
    operator fun invoke() {
        engagementRepository.endEngagement()
        chatScreenRepository.isFromCallScreen = false
    }
}
