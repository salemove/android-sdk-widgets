package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository
import io.reactivex.rxjava3.core.Flowable

internal interface OperatorTypingUseCase {
    operator fun invoke(): Flowable<Boolean>
}

internal class OperatorTypingUseCaseImpl(private val engagementRepository: EngagementRepository) : OperatorTypingUseCase {
    override fun invoke(): Flowable<Boolean> = engagementRepository.operatorTypingStatus
}
