package com.glia.widgets.core.chathead.domain

import com.glia.widgets.call.CallView
import com.glia.widgets.callvisualizer.EndScreenSharingView
import com.glia.widgets.chat.ChatView
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.filepreview.ui.FilePreviewView
import com.glia.widgets.helper.DialogHolderView
import com.glia.widgets.messagecenter.MessageCenterView

internal abstract class IsDisplayChatHeadUseCase(
    private val isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val screenSharingUseCase: ScreenSharingUseCase,
    val permissionManager: PermissionManager,
    private val configurationManager: GliaSdkConfigurationManager,
    private val engagementTypeUseCase: EngagementTypeUseCase
) {
    abstract fun isDisplayBasedOnPermission(): Boolean

    open operator fun invoke(viewName: String?): Boolean {
        return isBubbleEnabled() && isDisplayBasedOnPermission() && isShowForEngagement(viewName)
    }

    private fun isShowForEngagement(viewName: String?) =
        isShowForMediaEngagement(viewName) ||
            isShowForChatEngagement(viewName) ||
            isCallVisualizerScreenSharing(viewName)

    private fun isBubbleEnabled(): Boolean {
        return configurationManager.isUseOverlay
    }

    private fun isCallVisualizerScreenSharing(viewName: String?): Boolean {
        return isCurrentEngagementCallVisualizerUseCase() && screenSharingUseCase.isSharing && isNotInListOfGliaViews(viewName)
    }

    private fun isShowForMediaEngagement(viewName: String?): Boolean {
        return isMediaEngagementOrQueueingOngoing && isNotInListOfGliaViewsExceptChat(viewName)
    }

    private fun isShowForChatEngagement(viewName: String?): Boolean {
        return isChatEngagementOrQueueingOngoing && isNotInListOfGliaViews(viewName)
    }

    private fun isNotInListOfGliaViewsExceptChat(viewName: String?): Boolean {
        return viewName != CallView::class.java.simpleName &&
            viewName != FilePreviewView::class.java.simpleName &&
            viewName != EndScreenSharingView::class.java.simpleName &&
            viewName != MessageCenterView::class.java.simpleName &&
            viewName != DialogHolderView::class.java.simpleName
    }

    private fun isNotInListOfGliaViews(viewName: String?): Boolean {
        return viewName != ChatView::class.java.simpleName && isNotInListOfGliaViewsExceptChat(viewName)
    }

    private val isChatEngagementOrQueueingOngoing: Boolean get() = isChatEngagementOngoing || isChatQueueingOngoing
    private val isMediaEngagementOrQueueingOngoing: Boolean get() = isMediaEngagementOngoing || isMediaQueueingOngoing
    private val isMediaQueueingOngoing: Boolean get() = isQueueingOrEngagementUseCase.isQueueingForMedia
    private val isMediaEngagementOngoing: Boolean get() = engagementTypeUseCase.isMediaEngagement
    private val isChatQueueingOngoing: Boolean get() = isQueueingOrEngagementUseCase.isQueueingForChat
    private val isChatEngagementOngoing: Boolean get() = engagementTypeUseCase.isChatEngagement
}
