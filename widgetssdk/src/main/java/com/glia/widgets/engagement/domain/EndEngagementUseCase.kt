package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EndedBy
import com.glia.widgets.engagement.EngagementRepository

internal interface EndEngagementUseCase {
    operator fun invoke(endedBy: EndedBy)
}

internal class EndEngagementUseCaseImpl(private val engagementRepository: EngagementRepository) : EndEngagementUseCase {
    override fun invoke(endedBy: EndedBy) {
        if (engagementRepository.isQueueing) {
            engagementRepository.cancelQueuing()
        } else {
            engagementRepository.endEngagement(endedBy)
        }
    }
}
