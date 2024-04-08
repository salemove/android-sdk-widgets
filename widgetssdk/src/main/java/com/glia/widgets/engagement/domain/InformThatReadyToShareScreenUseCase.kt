package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EngagementRepository

internal interface InformThatReadyToShareScreenUseCase {
    operator fun invoke()
}

internal class InformThatReadyToShareScreenUseCaseImpl(private val engagementRepository: EngagementRepository) : InformThatReadyToShareScreenUseCase {
    override fun invoke() = engagementRepository.onReadyToShareScreen()
}
