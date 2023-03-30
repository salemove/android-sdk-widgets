package com.glia.widgets.core.notification.domain

import com.glia.widgets.core.notification.device.INotificationManager

class ShowScreenSharingNotificationUseCase(private val notificationManager: INotificationManager) {
    operator fun invoke() = notificationManager.showScreenSharingNotification()
}