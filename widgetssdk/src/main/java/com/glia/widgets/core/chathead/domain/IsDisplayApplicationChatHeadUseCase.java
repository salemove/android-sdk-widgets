package com.glia.widgets.core.chathead.domain;

import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.permissions.PermissionManager;
import com.glia.widgets.core.queue.GliaQueueRepository;

public class IsDisplayApplicationChatHeadUseCase {
    private final GliaEngagementRepository engagementRepository;
    private final GliaQueueRepository queueRepository;
    private final PermissionManager permissionManager;
    private final GliaSdkConfigurationManager configurationManager;

    public IsDisplayApplicationChatHeadUseCase(
            GliaEngagementRepository engagementRepository,
            GliaQueueRepository queueRepository,
            PermissionManager permissionManager,
            GliaSdkConfigurationManager configurationManager
    ) {
        this.engagementRepository = engagementRepository;
        this.queueRepository = queueRepository;
        this.permissionManager = permissionManager;
        this.configurationManager = configurationManager;
    }

    public boolean execute(
            boolean isChatView
    ) {
        return
                isBubbleEnabled() &&
                        isOverlayPermissionNotGiven() && (
                        isMediaEngagementOrQueueingInChat(isChatView) ||
                                isEngagementOrQueueingOutsideChatAndCallView(isChatView)
                );
    }

    private boolean isBubbleEnabled() {
        return this.configurationManager.isUseOverlay();
    }

    private boolean isOverlayPermissionNotGiven() {
        return !permissionManager.hasOverlayPermission();
    }

    private boolean isMediaQueueingOngoing() {
        return queueRepository.isMediaQueueingOngoing();
    }

    private boolean isMediaEngagementOngoing() {
        return engagementRepository.hasOngoingEngagement() &&
                engagementRepository.isMediaEngagement();
    }

    private boolean isEngagementOngoing() {
        return engagementRepository.hasOngoingEngagement();
    }

    private boolean isQueueingOngoing() {
        return !queueRepository.isNoQueueingOngoing();
    }

    private boolean isMediaEngagementOrQueueingInChat(boolean isChatView) {
        return isChatView && (isMediaEngagementOngoing() || isMediaQueueingOngoing());
    }

    private boolean isEngagementOrQueueingOutsideChatAndCallView(boolean isChatView) {
        return !isChatView && (isEngagementOngoing() || isQueueingOngoing());
    }
}
