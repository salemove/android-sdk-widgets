package com.glia.widgets.core.chathead.domain;

import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase;
import com.glia.widgets.engagement.domain.EngagementTypeUseCase;
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase;

/**
 * @hide
 */
public class ResolveChatHeadNavigationUseCase {
    private final IsQueueingOrEngagementUseCase isQueueingOrEngagementUseCase;
    private final EngagementTypeUseCase engagementTypeUseCase;
    private final IsCallVisualizerScreenSharingUseCase isCallVisualizerScreenSharingUseCase;

    public ResolveChatHeadNavigationUseCase(
        IsQueueingOrEngagementUseCase isQueueingOrEngagementUseCase,
        EngagementTypeUseCase engagementTypeUseCase,
        IsCallVisualizerScreenSharingUseCase isCallVisualizerScreenSharingUseCase
    ) {
        this.isQueueingOrEngagementUseCase = isQueueingOrEngagementUseCase;
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

    /**
     * @hide
     */
    public enum Destinations {
        CALL_VIEW,
        CHAT_VIEW,
        SCREEN_SHARING
    }

    private boolean isMediaQueueingOngoing() {
        return isQueueingOrEngagementUseCase.isQueueingForMedia();
    }

    private boolean isMediaEngagementOngoing() {
        return isQueueingOrEngagementUseCase.getHasOngoingEngagement() && engagementTypeUseCase.isMediaEngagement();
    }

    private boolean isSharingScreen() {
        return isCallVisualizerScreenSharingUseCase.invoke();
    }
}
