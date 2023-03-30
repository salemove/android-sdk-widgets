package com.glia.widgets.core.notification.domain

import com.glia.widgets.core.notification.device.INotificationManager

internal class ShowAudioCallNotificationUseCase(private val notificationManager: INotificationManager) {
    operator fun invoke() = notificationManager.showAudioCallNotification()
}