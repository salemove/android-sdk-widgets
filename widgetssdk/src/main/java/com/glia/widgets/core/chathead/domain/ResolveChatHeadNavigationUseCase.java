package com.glia.widgets.core.chathead.domain;

import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.GliaEngagementTypeRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.model.GliaQueueingState;

public class ResolveChatHeadNavigationUseCase {
    private final GliaEngagementRepository engagementRepository;
    private final GliaQueueRepository queueRepository;
    private final GliaEngagementTypeRepository gliaEngagementTypeRepository;
    private final IsCallVisualizerScreenSharingUseCase isCallVisualizerScreenSharingUseCase;

    public ResolveChatHeadNavigationUseCase(
            GliaEngagementRepository engagementRepository,
            GliaQueueRepository queueRepository,
            GliaEngagementTypeRepository gliaEngagementTypeRepository,
            IsCallVisualizerScreenSharingUseCase isCallVisualizerScreenSharingUseCase
    ) {
        this.engagementRepository = engagementRepository;
        this.queueRepository = queueRepository;
        this.gliaEngagementTypeRepository = gliaEngagementTypeRepository;
        this.isCallVisualizerScreenSharingUseCase = isCallVisualizerScreenSharingUseCase;
    }

    public Destinations execute() {
        if (isMediaEngagementOngoing() || isMediaQueueingOngoing()) {
            return Destinations.CALL_VIEW;
        } else if (isSharingScreen()) {
            return Destinations.SCREEN_SHARING;
        } else {
            return Destinations.CHAT_VIEW;
        }
    }

    public enum Destinations {
        CALL_VIEW,
        CHAT_VIEW,
        SCREEN_SHARING
    }

    private boolean isMediaQueueingOngoing() {
        return queueRepository.getQueueingState() instanceof GliaQueueingState.Media;
    }

    private boolean isMediaEngagementOngoing() {
        return engagementRepository.hasOngoingEngagement() && gliaEngagementTypeRepository.isMediaEngagement();
    }

    private boolean isSharingScreen() {
        return isCallVisualizerScreenSharingUseCase.invoke();
    }
}
