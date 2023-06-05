package com.glia.widgets.core.chathead.domain

import com.glia.widgets.core.chathead.ChatHeadManager
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.engagement.GliaEngagementRepository
import com.glia.widgets.core.engagement.GliaEngagementTypeRepository
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class ToggleChatHeadServiceUseCase(
    engagementRepository: GliaEngagementRepository,
    queueRepository: GliaQueueRepository,
    screenSharingRepository: GliaScreenSharingRepository,
    private val chatHeadManager: ChatHeadManager,
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
    override operator fun invoke(viewName: String?): Boolean {
        val isDisplayDeviceBubble = super.invoke(viewName)
        if (isDisplayDeviceBubble) {
            Logger.d(TAG, "Starting ChatHeadService")
            chatHeadManager.startChatHeadService()
        } else {
            Logger.d(TAG, "Stopping ChatHeadService")
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
