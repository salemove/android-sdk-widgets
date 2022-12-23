package com.glia.widgets.core.engagement.domain

import com.glia.widgets.core.engagement.GliaEngagementRepository
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.core.queue.model.GliaQueueingState

class IsOngoingEngagementUseCase(
    private val engagementRepository: GliaEngagementRepository
) {
    operator fun invoke(): Boolean {
        return engagementRepository.hasOngoingEngagement()
    }
}
