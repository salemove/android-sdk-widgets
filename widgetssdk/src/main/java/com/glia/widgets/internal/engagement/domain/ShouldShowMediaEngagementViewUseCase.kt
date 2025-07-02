package com.glia.widgets.internal.engagement.domain

import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase

internal class ShouldShowMediaEngagementViewUseCase(
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val engagementTypeUseCase: EngagementTypeUseCase
) {
    fun execute(isUpgradeToCall: Boolean): Boolean {
        return hasNoQueueingAndEngagementOngoing() ||
            hasMediaQueueingOngoing() ||
            hasOngoingMediaEngagement() ||
            isUpgradeToCall
    }

    private fun hasNoQueueingAndEngagementOngoing(): Boolean {
        return !isQueueingOrLiveEngagementUseCase.invoke()
    }

    private fun hasMediaQueueingOngoing(): Boolean {
        return isQueueingOrLiveEngagementUseCase.isQueueingForMedia
    }

    private fun hasOngoingMediaEngagement(): Boolean {
        return engagementTypeUseCase.isMediaEngagement
    }
}
