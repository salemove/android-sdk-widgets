package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository

internal interface IsQueueingOrEngagementUseCase {
    val hasOngoingEngagement: Boolean
    val isQueueingForMedia: Boolean
    val isQueueingForChat: Boolean
    operator fun invoke(): Boolean
}

internal class IsQueueingOrEngagementUseCaseImpl(private val engagementRepository: EngagementRepository) : IsQueueingOrEngagementUseCase {
    override val hasOngoingEngagement: Boolean get() = engagementRepository.hasOngoingEngagement
    override val isQueueingForMedia: Boolean get() = engagementRepository.isQueueingForMedia
    override val isQueueingForChat: Boolean get() = engagementRepository.isQueueing && !isQueueingForMedia
    override fun invoke(): Boolean = engagementRepository.isQueueingOrEngagement
}
