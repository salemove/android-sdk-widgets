package com.glia.widgets.core.engagement.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.GliaEngagementTypeRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.model.GliaQueueingState;

public class ShouldShowMediaEngagementViewUseCase {
    private final GliaEngagementRepository engagementRepository;
    private final GliaQueueRepository queueRepository;
    private final GliaEngagementTypeRepository gliaEngagementTypeRepository;

    public ShouldShowMediaEngagementViewUseCase(
            GliaEngagementRepository repository,
            GliaQueueRepository queueRepository,
            GliaEngagementTypeRepository gliaEngagementTypeRepository
    ) {
        this.engagementRepository = repository;
        this.queueRepository = queueRepository;
        this.gliaEngagementTypeRepository = gliaEngagementTypeRepository;
    }

    public boolean execute(boolean isUpgradeToCall) {
        return hasNoQueueingAndEngagementOngoing() || hasMediaQueueingOngoing() || hasOngoingMediaEngagement() || isUpgradeToCall;
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
        return gliaEngagementTypeRepository.isMediaEngagement();
    }

    private boolean hasNoOngoingEngagement() {
        return !engagementRepository.hasOngoingEngagement();
    }
}
