package com.glia.widgets.engagement.domain

import com.glia.androidsdk.EngagementRequest.Outcome
import com.glia.widgets.engagement.EngagementRepository
import io.reactivex.rxjava3.core.Flowable

internal interface OnIncomingEngagementRequestTimeoutUseCase {
    operator fun invoke(): Flowable<Unit>
}

internal class OnIncomingEngagementRequestTimeoutUseCaseImpl(
    private val engagementRepository: EngagementRepository
) : OnIncomingEngagementRequestTimeoutUseCase {

    override fun invoke(): Flowable<Unit> = engagementRepository.engagementOutcome
        .filter { it == Outcome.TIMEOUT }
        .map { Unit }
}
