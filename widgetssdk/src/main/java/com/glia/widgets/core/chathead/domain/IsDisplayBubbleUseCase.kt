package com.glia.widgets.core.chathead.domain

import com.glia.widgets.call.CallView
import com.glia.widgets.callvisualizer.EndScreenSharingView
import com.glia.widgets.chat.ChatView
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.filepreview.ui.ImagePreviewView
import com.glia.widgets.helper.DialogHolderView
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.messagecenter.MessageCenterView

internal abstract class IsDisplayBubbleUseCase(
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val screenSharingUseCase: ScreenSharingUseCase,
    val permissionManager: PermissionManager,
    internal val configurationManager: ConfigurationManager,
    private val engagementTypeUseCase: EngagementTypeUseCase
) {
    open operator fun invoke(viewName: String?): Boolean {

        val result = ((isOnChatScreenDuringMediaEngagement(viewName))
            || (isShowForEngagement() && isShowBasedOnForegroundBackground(viewName) && !isGliaView(viewName) && isBubbleEnabled()))
        return result
    }

    abstract fun isBubbleEnabled(): Boolean
    abstract fun isShowBasedOnForegroundBackground(viewName: String?): Boolean

    /*
    * The exception to all of the above is: If we’re looking at the chat screen during an A/V engagement, show the Bubble always irrespective of
    * Integrator settings. Clicking it takes you back to A/V.
    * */
    private fun isOnChatScreenDuringMediaEngagement(viewName: String?) =
        isMediaEngagementOngoing &&
            viewName == ChatView::class.java.simpleName &&
            isShowBasedOnForegroundBackground(viewName) // To avoid showing two bubbles, true on Chat screen only for app bubble

    private fun isShowForEngagement() =
        isMediaEngagementOrQueueingOngoing ||
            isChatEngagementOrQueueingOngoing ||
            isCallVisualizerScreenSharing()

    private fun isCallVisualizerScreenSharing(): Boolean {
        return isCurrentEngagementCallVisualizerUseCase() && screenSharingUseCase.isSharing
    }

    private fun isGliaView(viewName: String?): Boolean {
        return viewName == ChatView::class.java.simpleName ||
            viewName == CallView::class.java.simpleName ||
            viewName == ImagePreviewView::class.java.simpleName ||
            viewName == EndScreenSharingView::class.java.simpleName ||
            viewName == MessageCenterView::class.java.simpleName ||
            viewName == DialogHolderView::class.java.simpleName
    }

    private val isChatEngagementOrQueueingOngoing: Boolean get() = isChatEngagementOngoing || isChatQueueingOngoing
    private val isMediaEngagementOrQueueingOngoing: Boolean get() = isMediaEngagementOngoing || isMediaQueueingOngoing
    private val isMediaQueueingOngoing: Boolean get() = isQueueingOrLiveEngagementUseCase.isQueueingForMedia
    private val isMediaEngagementOngoing: Boolean get() = engagementTypeUseCase.isMediaEngagement
    private val isChatQueueingOngoing: Boolean get() = isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat
    private val isChatEngagementOngoing: Boolean get() = engagementTypeUseCase.isChatEngagement
}
