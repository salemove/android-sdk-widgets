package com.glia.widgets.core.chathead.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.GliaEngagementTypeRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.model.GliaQueueingState;

public class ResolveChatHeadNavigationUseCase {
    private final GliaEngagementRepository engagementRepository;
    private final GliaQueueRepository queueRepository;
    private final GliaEngagementTypeRepository gliaEngagementTypeRepository;

    public ResolveChatHeadNavigationUseCase(
            GliaEngagementRepository engagementRepository,
            GliaQueueRepository queueRepository,
            GliaEngagementTypeRepository gliaEngagementTypeRepository
    ) {
        this.engagementRepository = engagementRepository;
        this.queueRepository = queueRepository;
        this.gliaEngagementTypeRepository = gliaEngagementTypeRepository;
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
        return queueRepository.getQueueingState() instanceof GliaQueueingState.Media;
    }

    private boolean isMediaEngagementOngoing() {
        return engagementRepository.hasOngoingEngagement() && gliaEngagementTypeRepository.isMediaEngagement();
    }
}
