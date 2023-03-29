package com.glia.widgets.core.chathead.domain

import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.engagement.GliaEngagementRepository
import com.glia.widgets.core.engagement.GliaEngagementTypeRepository
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository

internal class IsDisplayApplicationChatHeadUseCase(
    engagementRepository: GliaEngagementRepository,
    queueRepository: GliaQueueRepository,
    screenSharingRepository: GliaScreenSharingRepository,
    permissionManager: PermissionManager,
    configurationManager: GliaSdkConfigurationManager,
    engagementTypeRepository: GliaEngagementTypeRepository
) : IsDisplayChatHeadUseCase(
    engagementRepository,
    queueRepository,
    screenSharingRepository,
    permissionManager,
    configurationManager,
    engagementTypeRepository
) {
    override fun isDisplayBasedOnPermission(): Boolean {
        return !permissionManager.hasOverlayPermission()
    }
}
