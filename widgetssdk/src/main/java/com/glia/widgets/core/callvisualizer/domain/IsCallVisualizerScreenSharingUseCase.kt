package com.glia.widgets.core.callvisualizer.domain

import com.glia.widgets.core.engagement.GliaEngagementTypeRepository

class IsCallVisualizerScreenSharingUseCase(private val engagementTypeRepository: GliaEngagementTypeRepository) {
    operator fun invoke(): Boolean = engagementTypeRepository.isCallVisualizerScreenSharing
}
