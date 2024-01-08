package com.glia.widgets.engagement.domain

import com.glia.androidsdk.Operator
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.Data
import io.reactivex.Flowable

internal interface CurrentOperatorUseCase {
    operator fun invoke(): Flowable<Operator>
}

internal class CurrentOperatorUseCaseImpl(private val engagementRepository: EngagementRepository) : CurrentOperatorUseCase {
    override fun invoke(): Flowable<Operator> = engagementRepository.currentOperator
        .filter(Data<Operator>::hasValue)
        .map { it as Data.Value }
        .map(Data.Value<Operator>::result)
}
