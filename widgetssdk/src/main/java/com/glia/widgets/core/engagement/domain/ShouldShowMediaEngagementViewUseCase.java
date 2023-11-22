package com.glia.widgets.core.engagement.domain;

import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.model.GliaQueueingState;
import com.glia.widgets.engagement.EngagementTypeUseCase;
import com.glia.widgets.engagement.HasOngoingEngagementUseCase;

public class ShouldShowMediaEngagementViewUseCase {
    private final HasOngoingEngagementUseCase hasOngoingEngagementUseCase;
    private final GliaQueueRepository queueRepository;
    private final EngagementTypeUseCase engagementTypeUseCase;

    public ShouldShowMediaEngagementViewUseCase(
        HasOngoingEngagementUseCase hasOngoingEngagementUseCase,
        GliaQueueRepository queueRepository,
        EngagementTypeUseCase engagementTypeUseCase) {
        this.hasOngoingEngagementUseCase = hasOngoingEngagementUseCase;
        this.queueRepository = queueRepository;
        this.engagementTypeUseCase = engagementTypeUseCase;
    }

    public boolean execute(boolean isUpgradeToCall) {
        return hasNoQueueingAndEngagementOngoing() ||
                hasMediaQueueingOngoing() ||
                hasOngoingMediaEngagement() ||
                isUpgradeToCall;
    }

    private boolean hasNoQueueingAndEngagementOngoing() {
        return hasNoQueueingOngoing() && hasNoOngoingEngagement();
    }

    private boolean hasNoQueueingOngoing() {
        return queueRepository.getQueueingState() instanceof GliaQueueingState.None;
    }

    private boolean hasMediaQueueingOngoing() {
        return queueRepository.getQueueingState() instanceof GliaQueueingState.Media;
    }

    private boolean hasOngoingMediaEngagement() {
        return engagementTypeUseCase.isMediaEngagement();
    }

    private boolean hasNoOngoingEngagement() {
        return !hasOngoingEngagementUseCase.invoke();
    }
}
