package com.glia.widgets.core.chathead.domain

import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository
import com.glia.widgets.engagement.EngagementTypeUseCase
import com.glia.widgets.engagement.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.IsQueueingOrEngagementUseCase

internal class IsDisplayApplicationChatHeadUseCase(
    isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase,
    isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    screenSharingRepository: GliaScreenSharingRepository,
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
    override fun isDisplayBasedOnPermission(): Boolean {
        return !permissionManager.hasOverlayPermission()
    }
}
