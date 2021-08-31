package com.glia.widgets.core.notification.domain;

import com.glia.widgets.core.notification.device.INotificationManager;

public class RemoveCallNotificationUseCase {
    private final INotificationManager notificationManager;

    public RemoveCallNotificationUseCase(INotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void execute() {
        notificationManager.removeCallNotification();
    }
}
