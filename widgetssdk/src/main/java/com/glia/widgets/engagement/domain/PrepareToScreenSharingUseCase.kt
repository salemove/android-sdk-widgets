package com.glia.widgets.engagement.domain

import android.os.Build
import com.glia.widgets.internal.notification.domain.ShowScreenSharingNotificationUseCase

internal interface PrepareToScreenSharingUseCase {
    operator fun invoke()
}

internal class PrepareToScreenSharingUseCaseImpl(
    private val showScreenSharingNotificationUseCase: ShowScreenSharingNotificationUseCase,
    private val startMediaProjectionServiceUseCase: StartMediaProjectionServiceUseCase,
    private val informThatReadyToShareScreenUseCase: InformThatReadyToShareScreenUseCase
) : PrepareToScreenSharingUseCase {
    override fun invoke() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMediaProjectionServiceUseCase()
        } else {
            informThatReadyToShareScreenUseCase()
            showScreenSharingNotificationUseCase()
        }
    }
}
