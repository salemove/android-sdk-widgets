package com.glia.widgets.core.notification.domain;

import com.glia.widgets.core.notification.device.INotificationManager;

public class ShowVideoCallNotificationUseCase {
    private final INotificationManager notificationManager;

    public ShowVideoCallNotificationUseCase(INotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void execute() {
        notificationManager.showVideoCallNotification();
    }
}
