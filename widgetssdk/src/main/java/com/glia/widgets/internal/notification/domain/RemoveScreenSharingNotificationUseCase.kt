package com.glia.widgets.internal.notification.domain

import com.glia.widgets.internal.notification.device.INotificationManager

internal class RemoveScreenSharingNotificationUseCase(private val notificationManager: INotificationManager) {
    operator fun invoke() = notificationManager.removeScreenSharingNotification()
}
