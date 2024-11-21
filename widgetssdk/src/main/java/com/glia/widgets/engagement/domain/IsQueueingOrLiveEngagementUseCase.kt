package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository

internal interface IsQueueingOrLiveEngagementUseCase {
    val hasOngoingLiveEngagement: Boolean
    val isQueueingForMedia: Boolean
    val isQueueingForLiveChat: Boolean
    operator fun invoke(): Boolean
}

internal class IsQueueingOrLiveEngagementUseCaseImpl(private val engagementRepository: EngagementRepository) : IsQueueingOrLiveEngagementUseCase {
    override val hasOngoingLiveEngagement: Boolean get() = engagementRepository.hasOngoingLiveEngagement
    override val isQueueingForMedia: Boolean get() = engagementRepository.isQueueingForMedia
    override val isQueueingForLiveChat: Boolean get() = engagementRepository.isQueueing && !isQueueingForMedia
    override fun invoke(): Boolean = engagementRepository.isQueueingOrLiveEngagement
}
