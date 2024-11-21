package com.glia.widgets.core.engagement.domain;

import com.glia.widgets.engagement.domain.EngagementTypeUseCase;
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase;

/**
 * @hide
 */
public class ShouldShowMediaEngagementViewUseCase {
    private final IsQueueingOrLiveEngagementUseCase isQueueingOrLiveEngagementUseCase;
    private final EngagementTypeUseCase engagementTypeUseCase;

    public ShouldShowMediaEngagementViewUseCase(
        IsQueueingOrLiveEngagementUseCase isQueueingOrLiveEngagementUseCase,
        EngagementTypeUseCase engagementTypeUseCase) {
        this.isQueueingOrLiveEngagementUseCase = isQueueingOrLiveEngagementUseCase;
        this.engagementTypeUseCase = engagementTypeUseCase;
    }

    public boolean execute(boolean isUpgradeToCall) {
        return hasNoQueueingAndEngagementOngoing() ||
            hasMediaQueueingOngoing() ||
            hasOngoingMediaEngagement() ||
            isUpgradeToCall;
    }

    private boolean hasNoQueueingAndEngagementOngoing() {
        return !isQueueingOrLiveEngagementUseCase.invoke();
    }

    private boolean hasMediaQueueingOngoing() {
        return isQueueingOrLiveEngagementUseCase.isQueueingForMedia();
    }

    private boolean hasOngoingMediaEngagement() {
        return engagementTypeUseCase.isMediaEngagement();
    }

}
