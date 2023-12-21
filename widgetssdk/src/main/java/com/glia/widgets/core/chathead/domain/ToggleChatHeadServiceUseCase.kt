package com.glia.widgets.core.chathead.domain

import com.glia.widgets.core.chathead.ChatHeadManager
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository
import com.glia.widgets.engagement.EngagementTypeUseCase
import com.glia.widgets.engagement.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.IsQueueingOrEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class ToggleChatHeadServiceUseCase(
    isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase,
    isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    screenSharingRepository: GliaScreenSharingRepository,
    private val chatHeadManager: ChatHeadManager,
    permissionManager: PermissionManager,
    configurationManager: GliaSdkConfigurationManager,
    engagementTypeUseCase: EngagementTypeUseCase
) : IsDisplayChatHeadUseCase(
    isQueueingOrEngagementUseCase,
    isCurrentEngagementCallVisualizerUseCase,
    screenSharingRepository,
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

    override fun isDisplayBasedOnPermission(): Boolean {
        return permissionManager.hasOverlayPermission()
    }

    fun onDestroy() {
        chatHeadManager.stopChatHeadService()
    }
}
