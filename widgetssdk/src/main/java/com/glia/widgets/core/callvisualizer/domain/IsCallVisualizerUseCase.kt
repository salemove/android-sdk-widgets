package com.glia.widgets.core.callvisualizer.domain

import com.glia.widgets.core.engagement.GliaEngagementRepository


class IsCallVisualizerUseCase(private val gliaEngagementRepository: GliaEngagementRepository) {
    operator fun invoke(): Boolean = gliaEngagementRepository.isCallVisualizerEngagement
}
