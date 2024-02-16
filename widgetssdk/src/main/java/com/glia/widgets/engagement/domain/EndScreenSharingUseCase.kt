package com.glia.widgets.engagement.domain

internal interface EndScreenSharingUseCase {
    operator fun invoke()
}

internal class EndScreenSharingUseCaseImpl(private val screenSharingUseCase: ScreenSharingUseCase) : EndScreenSharingUseCase {
    override fun invoke() = screenSharingUseCase.end()
}
