package com.glia.widgets.notification.domain;

import com.glia.widgets.notification.device.INotificationManager;

public class ShowScreenSharingNotificationUseCase {
    private final INotificationManager notificationManager;

    public ShowScreenSharingNotificationUseCase(INotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void execute() {
        this.notificationManager.showScreenSharingNotification();
    }
}
