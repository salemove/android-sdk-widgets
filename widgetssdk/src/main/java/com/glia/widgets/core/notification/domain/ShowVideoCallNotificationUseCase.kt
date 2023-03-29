package com.glia.widgets.core.notification.domain

import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerUseCase
import com.glia.widgets.core.notification.device.INotificationManager

internal class ShowVideoCallNotificationUseCase(
    private val notificationManager: INotificationManager,
    private val isCallVisualizerUseCase: IsCallVisualizerUseCase
) {
    operator fun invoke() = notificationManager.showVideoCallNotification(isCallVisualizerUseCase())
}