package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository

internal interface IsQueueingOrLiveEngagementUseCase {
    val hasOngoingLiveEngagement: Boolean
    val isQueueingForMedia: Boolean
    val isQueueingForAudio: Boolean
    val isQueueingForVideo: Boolean
    val isQueueingForLiveChat: Boolean
    val isQueueing: Boolean
    operator fun invoke(): Boolean
}

internal class IsQueueingOrLiveEngagementUseCaseImpl(private val engagementRepository: EngagementRepository) : IsQueueingOrLiveEngagementUseCase {
    override val hasOngoingLiveEngagement: Boolean get() = engagementRepository.hasOngoingLiveEngagement
    override val isQueueingForMedia: Boolean get() = engagementRepository.isQueueingForMedia
    override val isQueueingForAudio: Boolean get() = engagementRepository.isQueueingForAudio
    override val isQueueingForVideo: Boolean get() = engagementRepository.isQueueingForVideo
    override val isQueueingForLiveChat: Boolean get() = engagementRepository.isQueueing && !isQueueingForMedia
    override val isQueueing: Boolean get() = engagementRepository.isQueueing
    override fun invoke(): Boolean = engagementRepository.isQueueingOrLiveEngagement
}
