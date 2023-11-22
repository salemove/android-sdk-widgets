package com.glia.widgets.core.chathead.domain;

import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.model.GliaQueueingState;
import com.glia.widgets.engagement.EngagementTypeUseCase;
import com.glia.widgets.engagement.HasOngoingEngagementUseCase;

public class ResolveChatHeadNavigationUseCase {
    private final HasOngoingEngagementUseCase hasOngoingEngagementUseCase;
    private final GliaQueueRepository queueRepository;
    private final EngagementTypeUseCase engagementTypeUseCase;
    private final IsCallVisualizerScreenSharingUseCase isCallVisualizerScreenSharingUseCase;

    public ResolveChatHeadNavigationUseCase(
        HasOngoingEngagementUseCase hasOngoingEngagementUseCase,
        GliaQueueRepository queueRepository,
        EngagementTypeUseCase engagementTypeUseCase,
        IsCallVisualizerScreenSharingUseCase isCallVisualizerScreenSharingUseCase
    ) {
        this.hasOngoingEngagementUseCase = hasOngoingEngagementUseCase;
        this.queueRepository = queueRepository;
        this.engagementTypeUseCase = engagementTypeUseCase;
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
        return hasOngoingEngagementUseCase.invoke() && engagementTypeUseCase.isMediaEngagement();
    }

    private boolean isSharingScreen() {
        return isCallVisualizerScreenSharingUseCase.invoke();
    }
}
