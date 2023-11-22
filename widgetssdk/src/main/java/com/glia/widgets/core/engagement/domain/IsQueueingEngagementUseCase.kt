package com.glia.widgets.core.engagement.domain

import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.core.queue.model.GliaQueueingState

class IsQueueingEngagementUseCase(private val queueRepository: GliaQueueRepository) {
    operator fun invoke(): Boolean = queueRepository.queueingState !is GliaQueueingState.None
}
