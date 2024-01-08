package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository

internal interface IsOperatorPresentUseCase {
    operator fun invoke(): Boolean
}

internal class IsOperatorPresentUseCaseImpl(private val engagementRepository: EngagementRepository) : IsOperatorPresentUseCase {
    override fun invoke(): Boolean = engagementRepository.isOperatorPresent
}
