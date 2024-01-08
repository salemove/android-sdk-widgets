package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository

internal interface IsCurrentEngagementCallVisualizerUseCase {
    operator fun invoke(): Boolean
}

internal class IsCurrentEngagementCallVisualizerUseCaseImpl(private val engagementRepository: EngagementRepository) :
    IsCurrentEngagementCallVisualizerUseCase {
    override fun invoke(): Boolean = engagementRepository.isCallVisualizerEngagement
}
