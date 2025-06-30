package com.glia.widgets.internal.chathead.domain

import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.launcher.ConfigurationManager

internal class IsDisplayBubbleInsideAppUseCase(
    isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    permissionManager: PermissionManager,
    configurationManager: ConfigurationManager,
    engagementTypeUseCase: EngagementTypeUseCase
) : IsDisplayBubbleUseCase(
    isQueueingOrLiveEngagementUseCase,
    permissionManager,
    configurationManager,
    engagementTypeUseCase
) {
    override fun isBubbleEnabled(): Boolean {
        return configurationManager.enableBubbleInsideApp
    }

    override fun isShowBasedOnForegroundBackground(viewName: String?): Boolean {
        return viewName != null && // App is in foreground
            // Use only ChatHeadService instead of ChatHeadService + app bubble if bubble is enabled outside and inside
            (!configurationManager.enableBubbleOutsideApp || !configurationManager.enableBubbleInsideApp || !permissionManager.hasOverlayPermission())
    }
}
