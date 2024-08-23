package com.glia.widgets.core.chathead.domain

import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase

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
    override fun isBubbleEnabled(): Boolean {
        return configurationManager.isEnableBubbleInsideApp
    }

    override fun isShowBasedOnForegroundBackground(viewName: String?): Boolean {
        return viewName != null && // App is in foreground
            // Use only ChatHeadService instead of ChatHeadService + app bubble if bubble is enabled outside and inside
            (!configurationManager.isEnableBubbleOutsideApp || !configurationManager.isEnableBubbleInsideApp || !permissionManager.hasOverlayPermission())
    }
}
