package com.glia.widgets.internal.chathead.domain

import com.glia.widgets.call.CallView
import com.glia.widgets.chat.ChatView
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.filepreview.ui.ImagePreviewView
import com.glia.widgets.helper.DialogHolderView
import com.glia.widgets.messagecenter.MessageCenterView

internal class IsBubbleNeededUseCase(
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val engagementTypeUseCase: EngagementTypeUseCase
) {
    private val isBubbleNeededByEngagement: Boolean
        get() = isQueueingOrLiveEngagementUseCase.isQueueingForMedia ||
            isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat ||
            engagementTypeUseCase.isMediaEngagement ||
            engagementTypeUseCase.isChatEngagement

    private val excludedScreensForBubble: Set<String> = setOf(
        ChatView::class.java.simpleName,
        CallView::class.java.simpleName,
        ImagePreviewView::class.java.simpleName,
        MessageCenterView::class.java.simpleName,
        DialogHolderView::class.java.simpleName
    )

    operator fun invoke(viewName: String?): Boolean {
        val isBubbleNeeded = isBubbleNeededByEngagement && isBubbleNeededByScreen(viewName)

        return isBubbleNeeded
    }

    fun isBubbleNeededByChatScreenDuringMediaEngagement(viewName: String?) =
        engagementTypeUseCase.isMediaEngagement && viewName == ChatView::class.java.simpleName

    private fun isBubbleNeededByScreen(viewName: String?): Boolean =
        viewName !in excludedScreensForBubble || isBubbleNeededByChatScreenDuringMediaEngagement(viewName)

}
