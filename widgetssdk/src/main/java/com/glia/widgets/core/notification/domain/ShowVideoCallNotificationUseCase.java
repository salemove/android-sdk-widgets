package com.glia.widgets.core.notification.domain;

import com.glia.widgets.core.engagement.domain.IsCallVisualizerUseCase;
import com.glia.widgets.core.notification.device.INotificationManager;

public class ShowVideoCallNotificationUseCase {
    private final INotificationManager notificationManager;
    private final IsCallVisualizerUseCase isCallVisualizerUseCase;

    public ShowVideoCallNotificationUseCase(
            INotificationManager notificationManager,
            IsCallVisualizerUseCase isCallVisualizerUseCase
    ) {
        this.notificationManager = notificationManager;
        this.isCallVisualizerUseCase = isCallVisualizerUseCase;
    }

    public void execute() {
        notificationManager.showVideoCallNotification(isCallVisualizerUseCase.execute());
    }
}
