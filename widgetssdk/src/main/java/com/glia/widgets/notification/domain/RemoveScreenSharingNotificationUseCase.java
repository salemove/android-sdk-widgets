package com.glia.widgets.notification.domain;

import com.glia.widgets.notification.device.INotificationManager;

public class RemoveScreenSharingNotificationUseCase {
    private final INotificationManager notificationManager;

    public RemoveScreenSharingNotificationUseCase(INotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void execute() {
        this.notificationManager.removeScreenSharingNotification();
    }
}
