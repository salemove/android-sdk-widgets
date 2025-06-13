package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository

internal interface EndEngagementUseCase {
    operator fun invoke()
    fun silently()
}

internal class EndEngagementUseCaseImpl(private val engagementRepository: EngagementRepository) : EndEngagementUseCase {
    override fun invoke() = cancelQueueingOrElse {
        engagementRepository.endEngagement()
    }

    override fun silently() = cancelQueueingOrElse {
        engagementRepository.terminateEngagement()
    }

    private fun cancelQueueingOrElse(callback: () -> Unit) {
        if (engagementRepository.isQueueing) {
            engagementRepository.cancelQueuing()
        } else {
            callback()
        }
    }
}
