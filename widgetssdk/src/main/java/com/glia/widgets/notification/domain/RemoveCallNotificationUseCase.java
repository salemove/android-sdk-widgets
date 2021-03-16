package com.glia.widgets.notification.domain;

import com.glia.widgets.notification.device.INotificationManager;

public class RemoveCallNotificationUseCase {
    private final INotificationManager notificationManager;

    public RemoveCallNotificationUseCase(INotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void execute() {
        notificationManager.removeCallNotification();
    }
}
