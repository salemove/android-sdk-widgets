package com.glia.widgets.core.callvisualizer.domain

import com.glia.widgets.engagement.EngagementTypeUseCase

internal class IsCallVisualizerScreenSharingUseCase(private val engagementTypeUseCase: EngagementTypeUseCase) {
    operator fun invoke(): Boolean = engagementTypeUseCase.isCallVisualizerScreenSharing
}
