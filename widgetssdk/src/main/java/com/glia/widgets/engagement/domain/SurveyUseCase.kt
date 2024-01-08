package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.SurveyState
import io.reactivex.Flowable

internal interface SurveyUseCase {
    operator fun invoke(): Flowable<SurveyState>
}

internal class SurveyUseCaseImpl(private val engagementRepository: EngagementRepository):SurveyUseCase {
    override fun invoke(): Flowable<SurveyState> = engagementRepository.survey
}
