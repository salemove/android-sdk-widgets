package com.glia.widgets.core.chathead.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.GliaEngagementTypeRepository;
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerUseCase;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.model.GliaQueueingState;
import com.glia.widgets.di.Dependencies;

public class ResolveChatHeadNavigationUseCase {
    private final GliaEngagementRepository engagementRepository;
    private final GliaQueueRepository queueRepository;
    private final GliaEngagementTypeRepository gliaEngagementTypeRepository;
    private final IsCallVisualizerUseCase isCallVisualizerUseCase;

    public ResolveChatHeadNavigationUseCase(
            GliaEngagementRepository engagementRepository,
            GliaQueueRepository queueRepository,
            GliaEngagementTypeRepository gliaEngagementTypeRepository,
            IsCallVisualizerUseCase isCallVisualizerUseCase
    ) {
        this.engagementRepository = engagementRepository;
        this.queueRepository = queueRepository;
        this.gliaEngagementTypeRepository = gliaEngagementTypeRepository;
        this.isCallVisualizerUseCase = isCallVisualizerUseCase;
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
        return Dependencies.getUseCaseFactory().createIsCallVisualizerUseCase().execute();
    }
}
