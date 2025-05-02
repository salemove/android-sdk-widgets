package com.glia.widgets.internal.chathead.domain

import com.glia.widgets.internal.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase

internal class ResolveChatHeadNavigationUseCase(
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val engagementTypeUseCase: EngagementTypeUseCase,
    private val isCallVisualizerScreenSharingUseCase: IsCallVisualizerScreenSharingUseCase
) {
    fun execute(): Destinations {
        return if (isMediaEngagementOngoing || isMediaQueueingOngoing) {
            Destinations.CALL_VIEW
        } else if (isSharingScreen) {
            Destinations.SCREEN_SHARING
        } else {
            Destinations.CHAT_VIEW
        }
    }

    enum class Destinations {
        CALL_VIEW,
        CHAT_VIEW,
        SCREEN_SHARING
    }

    private val isMediaQueueingOngoing: Boolean
        get() = isQueueingOrLiveEngagementUseCase.isQueueingForMedia

    private val isMediaEngagementOngoing: Boolean
        get() = isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement && engagementTypeUseCase.isMediaEngagement

    private val isSharingScreen: Boolean
        get() = isCallVisualizerScreenSharingUseCase.invoke()
}
