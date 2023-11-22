package com.glia.widgets.core.chathead.domain

import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository
import com.glia.widgets.engagement.EngagementTypeUseCase
import com.glia.widgets.engagement.HasOngoingEngagementUseCase
import com.glia.widgets.engagement.IsCurrentEngagementCallVisualizer

internal class IsDisplayApplicationChatHeadUseCase(
    hasOngoingEngagementUseCase: HasOngoingEngagementUseCase,
    isCurrentEngagementCallVisualizer: IsCurrentEngagementCallVisualizer,
    queueRepository: GliaQueueRepository,
    screenSharingRepository: GliaScreenSharingRepository,
    permissionManager: PermissionManager,
    configurationManager: GliaSdkConfigurationManager,
    engagementTypeUseCase: EngagementTypeUseCase
) : IsDisplayChatHeadUseCase(
    hasOngoingEngagementUseCase,
    isCurrentEngagementCallVisualizer,
    queueRepository,
    screenSharingRepository,
    permissionManager,
    configurationManager,
    engagementTypeUseCase
) {
    override fun isDisplayBasedOnPermission(): Boolean {
        return !permissionManager.hasOverlayPermission()
    }
}
