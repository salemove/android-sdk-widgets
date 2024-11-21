package com.glia.widgets.core.chathead.domain;

import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase;
import com.glia.widgets.engagement.domain.EngagementTypeUseCase;
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase;

/**
 * @hide
 */
public class ResolveChatHeadNavigationUseCase {
    private final IsQueueingOrLiveEngagementUseCase isQueueingOrLiveEngagementUseCase;
    private final EngagementTypeUseCase engagementTypeUseCase;
    private final IsCallVisualizerScreenSharingUseCase isCallVisualizerScreenSharingUseCase;

    public ResolveChatHeadNavigationUseCase(
        IsQueueingOrLiveEngagementUseCase isQueueingOrLiveEngagementUseCase,
        EngagementTypeUseCase engagementTypeUseCase,
        IsCallVisualizerScreenSharingUseCase isCallVisualizerScreenSharingUseCase
    ) {
        this.isQueueingOrLiveEngagementUseCase = isQueueingOrLiveEngagementUseCase;
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
        return isQueueingOrLiveEngagementUseCase.isQueueingForMedia();
    }

    private boolean isMediaEngagementOngoing() {
        return isQueueingOrLiveEngagementUseCase.getHasOngoingLiveEngagement() && engagementTypeUseCase.isMediaEngagement();
    }

    private boolean isSharingScreen() {
        return isCallVisualizerScreenSharingUseCase.invoke();
    }
}
