package com.glia.widgets.internal.chathead.domain

import com.glia.widgets.internal.chathead.ChatHeadManager
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.launcher.ConfigurationManager

/**
 * This use case:
 * 1) determines whether the chat head (bubble) should be displayed
 * 2) starts or stops the chat head service (bubble outside the app)
 */
internal class IsDisplayBubbleOutsideAppUseCase(
    isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    screenSharingUseCase: ScreenSharingUseCase,
    private val chatHeadManager: ChatHeadManager,
    permissionManager: PermissionManager,
    configurationManager: ConfigurationManager,
    engagementTypeUseCase: EngagementTypeUseCase
) : IsDisplayBubbleUseCase(
    isQueueingOrLiveEngagementUseCase,
    isCurrentEngagementCallVisualizerUseCase,
    screenSharingUseCase,
    permissionManager,
    configurationManager,
    engagementTypeUseCase
) {
    override operator fun invoke(viewName: String?): Boolean {
        val isDisplayDeviceBubble = super.invoke(viewName)
        if (isDisplayDeviceBubble) {
            Logger.i(TAG, "Bubble: show device bubble")
            chatHeadManager.startChatHeadService()
        } else {
            Logger.d(TAG, "Bubble: hide device bubble")
            chatHeadManager.stopChatHeadService()
        }
        return isDisplayDeviceBubble
    }

    override fun isBubbleEnabled(): Boolean {
        // "Global" device bubble should be displayed only when allowed.
        //
        // So, isBubbleEnabled() for ToggleChatHeadServiceUseCase returns true when:
        // global device bubble is NOT turned off by integrator (turned ON by default)
        // AND
        // global device bubble is allowed by visitor (overlay permission).
        return configurationManager.enableBubbleOutsideApp && permissionManager.hasOverlayPermission()
    }

    override fun isShowBasedOnForegroundBackground(viewName: String?): Boolean {
        return viewName == null || // true when app is in background
            // Use only ChatHeadService instead of ChatHeadService + app bubble if bubble is enabled outside and inside
            (configurationManager.enableBubbleOutsideApp && configurationManager.enableBubbleInsideApp && permissionManager.hasOverlayPermission())
    }

    fun onDestroy() {
        chatHeadManager.stopChatHeadService()
    }
}
