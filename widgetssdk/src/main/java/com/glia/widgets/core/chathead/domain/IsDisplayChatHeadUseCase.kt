package com.glia.widgets.core.chathead.domain

import com.glia.widgets.call.CallView
import com.glia.widgets.callvisualizer.EndScreenSharingView
import com.glia.widgets.chat.ChatView
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.engagement.GliaEngagementRepository
import com.glia.widgets.core.engagement.GliaEngagementTypeRepository
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.core.queue.model.GliaQueueingState
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository
import com.glia.widgets.filepreview.ui.FilePreviewView
import com.glia.widgets.messagecenter.MessageCenterView

internal abstract class IsDisplayChatHeadUseCase(
    val engagementRepository: GliaEngagementRepository,
    private val queueRepository: GliaQueueRepository,
    private val screenSharingRepository: GliaScreenSharingRepository,
    val permissionManager: PermissionManager,
    private val configurationManager: GliaSdkConfigurationManager,
    private val engagementTypeRepository: GliaEngagementTypeRepository
) {

    abstract fun isDisplayBasedOnPermission(): Boolean

    open operator fun invoke(viewName: String?): Boolean {
        return (isBubbleEnabled() && isDisplayBasedOnPermission() && isShowForEngagement(viewName))
    }

    private fun isShowForEngagement(viewName: String?) =
        isShowForMediaEngagement(viewName) ||
            isShowForChatEngagement(viewName) ||
            isCallVisualizerScreenSharing(viewName)

    private fun isBubbleEnabled(): Boolean {
        return configurationManager.isUseOverlay
    }

    private fun isCallVisualizerScreenSharing(viewName: String?): Boolean {
        return engagementRepository.isCallVisualizerEngagement &&
            screenSharingRepository.isSharingScreen && isNotInListOfGliaViews(viewName)
    }

    private fun isShowForMediaEngagement(viewName: String?): Boolean {
        return isMediaEngagementOrQueueingOngoing() && isNotInListOfGliaViewsExceptChat(viewName)
    }

    private fun isShowForChatEngagement(viewName: String?): Boolean {
        return isChatEngagementOrQueueingOngoing() && isNotInListOfGliaViews(viewName)
    }

    private fun isNotInListOfGliaViewsExceptChat(viewName: String?): Boolean {
        return viewName != CallView::class.java.simpleName &&
            viewName != FilePreviewView::class.java.simpleName &&
            viewName != EndScreenSharingView::class.java.simpleName &&
            viewName != MessageCenterView::class.java.simpleName
    }

    private fun isNotInListOfGliaViews(viewName: String?): Boolean {
        return viewName != ChatView::class.java.simpleName && isNotInListOfGliaViewsExceptChat(viewName)
    }

    private fun isChatEngagementOrQueueingOngoing(): Boolean {
        return isChatEngagementOngoing() || isChatQueueingOngoing()
    }

    private fun isMediaEngagementOrQueueingOngoing(): Boolean {
        return isMediaEngagementOngoing() || isMediaQueueingOngoing()
    }

    private fun isMediaQueueingOngoing(): Boolean {
        val state = queueRepository.queueingState
        return state is GliaQueueingState.Media
    }

    private fun isMediaEngagementOngoing(): Boolean {
        return engagementRepository.hasOngoingEngagement() && engagementTypeRepository.isMediaEngagement
    }

    private fun isChatQueueingOngoing(): Boolean {
        return queueRepository.queueingState is GliaQueueingState.Chat
    }

    private fun isChatEngagementOngoing(): Boolean {
        return engagementRepository.hasOngoingEngagement() && engagementTypeRepository.isChatEngagement
    }
}
