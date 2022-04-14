package com.glia.widgets.core.chathead.domain;

import android.view.View;

import com.glia.widgets.call.CallView;
import com.glia.widgets.chat.ChatView;
import com.glia.widgets.core.chathead.ChatHeadManager;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.permissions.PermissionManager;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.filepreview.ui.FilePreviewView;

public class ToggleChatHeadServiceUseCase {
    private final GliaEngagementRepository engagementRepository;
    private final GliaQueueRepository queueRepository;
    private final ChatHeadManager chatHeadManager;
    private final PermissionManager permissionManager;
    private final GliaSdkConfigurationManager configurationManager;

    public ToggleChatHeadServiceUseCase(
            GliaEngagementRepository engagementRepository,
            GliaQueueRepository queueRepository,
            ChatHeadManager chatHeadManager,
            PermissionManager permissionManager,
            GliaSdkConfigurationManager configurationManager
    ) {
        this.engagementRepository = engagementRepository;
        this.queueRepository = queueRepository;
        this.chatHeadManager = chatHeadManager;
        this.permissionManager = permissionManager;
        this.configurationManager = configurationManager;
    }

    public void execute(View view) {
        if (isBubbleEnabled() && hasOverlayPermission() && (isShowForMediaEngagement(view) || isShowForChatEngagement(view))) {
            chatHeadManager.startChatHeadService();
        } else {
            chatHeadManager.stopChatHeadService();
        }
    }

    private boolean isBubbleEnabled() {
        return configurationManager.isUseOverlay();
    }

    private boolean hasOverlayPermission() {
        return permissionManager.hasOverlayPermission();
    }

    private boolean isShowForMediaEngagement(View view) {
        return isNotInCallView(view) && isMediaEngagementOrQueueingOngoing();
    }

    private boolean isShowForChatEngagement(View view) {
        return isChatEngagementOrQueueingOngoing() && isNotInChatView(view) && isNotInCallView(view) && isNotInFilePreviewView(view);
    }

    private boolean isChatEngagementOrQueueingOngoing() {
        return isChatEngagementOngoing() || isChatQueueingOngoing();
    }

    private boolean isMediaEngagementOrQueueingOngoing() {
        return isMediaEngagementOngoing() || isMediaQueueingOngoing();
    }

    private boolean isNotInCallView(View view) {
        return !(view instanceof CallView);
    }

    private boolean isNotInChatView(View view) {
        return !(view instanceof ChatView);
    }

    private boolean isNotInFilePreviewView(View view) {
        return !(view instanceof FilePreviewView);
    }

    private boolean isMediaQueueingOngoing() {
        return queueRepository.isMediaQueueingOngoing();
    }

    private boolean isMediaEngagementOngoing() {
        return engagementRepository.hasOngoingEngagement() && engagementRepository.isMediaEngagement();
    }

    private boolean isChatQueueingOngoing() {
        return queueRepository.isChatQueueingOngoing();
    }

    private boolean isChatEngagementOngoing() {
        return engagementRepository.hasOngoingEngagement() && engagementRepository.isChatEngagement();
    }
}
