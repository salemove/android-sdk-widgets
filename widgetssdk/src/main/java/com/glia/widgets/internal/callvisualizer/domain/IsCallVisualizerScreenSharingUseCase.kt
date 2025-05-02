package com.glia.widgets.internal.callvisualizer.domain

import com.glia.widgets.engagement.domain.EngagementTypeUseCase

internal class IsCallVisualizerScreenSharingUseCase(private val engagementTypeUseCase: EngagementTypeUseCase) {
    operator fun invoke(): Boolean = engagementTypeUseCase.isCallVisualizerScreenSharing
}
