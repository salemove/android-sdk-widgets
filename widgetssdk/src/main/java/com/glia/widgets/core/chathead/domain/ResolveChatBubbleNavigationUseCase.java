package com.glia.widgets.core.chathead.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;

public class ResolveChatBubbleNavigationUseCase {
    private final GliaEngagementRepository engagementRepository;
    private final GliaQueueRepository queueRepository;

    public ResolveChatBubbleNavigationUseCase(
            GliaEngagementRepository engagementRepository,
            GliaQueueRepository queueRepository
    ) {
        this.engagementRepository = engagementRepository;
        this.queueRepository = queueRepository;
    }

    public Destinations execute() {
        if (isMediaEngagementOngoing() || isMediaQueueingOngoing()) {
            return Destinations.CALL_VIEW;
        } else {
            return Destinations.CHAT_VIEW;
        }
    }

    public enum Destinations {
        CALL_VIEW,
        CHAT_VIEW
    }

    private boolean isMediaQueueingOngoing() {
        return queueRepository.isMediaQueueingOngoing();
    }

    private boolean isMediaEngagementOngoing() {
        return engagementRepository.hasOngoingEngagement() && engagementRepository.isMediaEngagement();
    }
}
