package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository

internal interface EndEngagementUseCase {
    operator fun invoke(silently: Boolean = false)
}

internal class EndEngagementUseCaseImpl(private val engagementRepository: EngagementRepository) : EndEngagementUseCase {
    override fun invoke(silently: Boolean) {
        if (engagementRepository.isQueueing) {
            engagementRepository.cancelQueuing()
        } else {
            engagementRepository.endEngagement(silently)
        }
    }
}
