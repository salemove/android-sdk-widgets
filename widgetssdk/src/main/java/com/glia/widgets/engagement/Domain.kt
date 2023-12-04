package com.glia.widgets.engagement

import com.glia.widgets.helper.OneTimeEvent
import io.reactivex.Flowable

internal class EndEngagementUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(silently: Boolean = false) = engagementRepository.endEngagement(silently)
}

internal class HasOngoingEngagementUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Boolean = engagementRepository.hasOngoingEngagement
}

internal class IsCurrentEngagementCallVisualizer(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Boolean = engagementRepository.isCallVisualizerEngagement
}

internal class SurveyUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<OneTimeEvent<EngagementRepository.SurveyState>> = engagementRepository.survey.map(::OneTimeEvent)
}

internal class EngagementStateUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<EngagementRepository.State> = engagementRepository.engagementState
}
