package com.glia.widgets.core.engagement.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;

public class ShouldShowMediaEngagementViewUseCase {
    private final GliaEngagementRepository engagementRepository;
    private final GliaOperatorMediaRepository operatorMediaRepository;
    private final GliaQueueRepository queueRepository;

    public ShouldShowMediaEngagementViewUseCase(
            GliaEngagementRepository repository,
            GliaOperatorMediaRepository operatorMediaRepository,
            GliaQueueRepository queueRepository
    ) {
        this.engagementRepository = repository;
        this.operatorMediaRepository = operatorMediaRepository;
        this.queueRepository = queueRepository;
    }

    public boolean execute() {
        return isNoQueueingAndIsNoEngagementOrIsMediaEngagement() || isMediaQueueing();
    }

    private boolean isNoQueueingAndIsNoEngagementOrIsMediaEngagement() {
        return isNoQueueing() && (
                hasOngoingMediaEngagement() ||
                        hasNoOngoingEngagement());
    }

    private boolean isNoQueueing() {
        return queueRepository.isNoQueueingOngoing();
    }

    private boolean isMediaQueueing() {
        return queueRepository.isMediaQueueingOngoing();
    }

    private boolean hasOngoingMediaEngagement() {
        return engagementRepository.hasOngoingEngagement() &&
                (engagementRepository.isMediaEngagement() || operatorMediaRepository.isOperatorMediaState());
    }

    private boolean hasNoOngoingEngagement() {
        return !engagementRepository.hasOngoingEngagement();
    }
}
