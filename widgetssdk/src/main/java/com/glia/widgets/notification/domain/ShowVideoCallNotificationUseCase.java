package com.glia.widgets.notification.domain;

import com.glia.widgets.notification.device.INotificationManager;

public class ShowVideoCallNotificationUseCase {
    private final INotificationManager notificationManager;

    public ShowVideoCallNotificationUseCase(INotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void execute() {
        notificationManager.showVideoCallNotification();
    }
}
