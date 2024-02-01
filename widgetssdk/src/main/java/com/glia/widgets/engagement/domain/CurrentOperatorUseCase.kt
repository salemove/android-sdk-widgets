package com.glia.widgets.engagement.domain

import com.glia.androidsdk.Operator
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.formattedName
import io.reactivex.Flowable

internal interface CurrentOperatorUseCase {
    val formattedName: Flowable<String>
    operator fun invoke(): Flowable<Operator>
}

internal class CurrentOperatorUseCaseImpl(engagementRepository: EngagementRepository) : CurrentOperatorUseCase {
    private val currentOperator = engagementRepository.currentOperator
        .filter(Data<Operator>::hasValue)
        .map { it as Data.Value }
        .map(Data.Value<Operator>::result)

    override val formattedName: Flowable<String> = currentOperator.map(Operator::formattedName)

    override fun invoke(): Flowable<Operator> = currentOperator
}
