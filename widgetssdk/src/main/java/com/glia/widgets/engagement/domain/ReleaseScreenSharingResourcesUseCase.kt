package com.glia.widgets.engagement.domain

import android.os.Build
import com.glia.widgets.internal.notification.domain.RemoveScreenSharingNotificationUseCase

internal interface ReleaseScreenSharingResourcesUseCase {
    operator fun invoke()
}

internal class ReleaseScreenSharingResourcesUseCaseImpl(
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase,
    private val stopMediaProjectionServiceUseCase: StopMediaProjectionServiceUseCase
) : ReleaseScreenSharingResourcesUseCase {
    override fun invoke() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopMediaProjectionServiceUseCase()
        } else {
            removeScreenSharingNotificationUseCase()
        }
    }
}
