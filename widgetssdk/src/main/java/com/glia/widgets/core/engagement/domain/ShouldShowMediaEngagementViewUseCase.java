package com.glia.widgets.core.engagement.domain;

import com.glia.widgets.engagement.domain.EngagementTypeUseCase;
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase;

/**
 * @hide
 */
public class ShouldShowMediaEngagementViewUseCase {
    private final IsQueueingOrEngagementUseCase isQueueingOrEngagementUseCase;
    private final EngagementTypeUseCase engagementTypeUseCase;

    public ShouldShowMediaEngagementViewUseCase(
        IsQueueingOrEngagementUseCase isQueueingOrEngagementUseCase,
        EngagementTypeUseCase engagementTypeUseCase) {
        this.isQueueingOrEngagementUseCase = isQueueingOrEngagementUseCase;
        this.engagementTypeUseCase = engagementTypeUseCase;
    }

    public boolean execute(boolean isUpgradeToCall) {
        return hasNoQueueingAndEngagementOngoing() ||
            hasMediaQueueingOngoing() ||
            hasOngoingMediaEngagement() ||
            isUpgradeToCall;
    }

    private boolean hasNoQueueingAndEngagementOngoing() {
        return !isQueueingOrEngagementUseCase.invoke();
    }

    private boolean hasMediaQueueingOngoing() {
        return isQueueingOrEngagementUseCase.isQueueingForMedia();
    }

    private boolean hasOngoingMediaEngagement() {
        return engagementTypeUseCase.isMediaEngagement();
    }

}
