package com.glia.widgets.core.notification.domain;

import com.glia.widgets.core.notification.device.INotificationManager;

public class ShowScreenSharingNotificationUseCase {
    private final INotificationManager notificationManager;

    public ShowScreenSharingNotificationUseCase(INotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void execute() {
        this.notificationManager.showScreenSharingNotification();
    }
}
