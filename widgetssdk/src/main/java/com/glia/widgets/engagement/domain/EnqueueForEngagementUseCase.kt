package com.glia.widgets.engagement.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.engagement.EngagementRepository

internal interface EnqueueForEngagementUseCase {
    operator fun invoke(mediaType: Engagement.MediaType = Engagement.MediaType.TEXT)
}

internal class EnqueueForEngagementUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val queueRepository: QueueRepository
) : EnqueueForEngagementUseCase {
    override fun invoke(mediaType: Engagement.MediaType) = engagementRepository.queueForEngagement(queueRepository.relevantQueueIds, mediaType)
}
