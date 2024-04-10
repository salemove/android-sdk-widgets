package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.State
import io.reactivex.rxjava3.core.Flowable

internal interface EngagementStateUseCase {
    operator fun invoke(): Flowable<State>
}

internal class EngagementStateUseCaseImpl(private val engagementRepository: EngagementRepository) : EngagementStateUseCase {
    override fun invoke(): Flowable<State> = engagementRepository.engagementState
}
