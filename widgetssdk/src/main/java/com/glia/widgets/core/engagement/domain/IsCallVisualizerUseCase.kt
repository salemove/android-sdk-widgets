package com.glia.widgets.core.engagement.domain

import com.glia.widgets.core.engagement.GliaEngagementRepository


class IsCallVisualizerUseCase(
    private val gliaEngagementRepository: GliaEngagementRepository
) {
    fun execute(): Boolean {
        return gliaEngagementRepository.isCallVisualizerEngagement
    }
}
