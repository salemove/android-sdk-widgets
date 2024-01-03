package com.glia.widgets.core.chathead.domain

import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.engagement.EngagementTypeUseCase
import com.glia.widgets.engagement.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.IsQueueingOrEngagementUseCase
import com.glia.widgets.engagement.ScreenSharingUseCase

internal class IsDisplayApplicationChatHeadUseCase(
    isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase,
    isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    screenSharingUseCase: ScreenSharingUseCase,
    permissionManager: PermissionManager,
    configurationManager: GliaSdkConfigurationManager,
    engagementTypeUseCase: EngagementTypeUseCase
) : IsDisplayChatHeadUseCase(
    isQueueingOrEngagementUseCase,
    isCurrentEngagementCallVisualizerUseCase,
    screenSharingUseCase,
    permissionManager,
    configurationManager,
    engagementTypeUseCase
) {
    override fun isDisplayBasedOnPermission(): Boolean {
        return !permissionManager.hasOverlayPermission()
    }
}
