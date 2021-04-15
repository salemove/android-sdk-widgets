package com.glia.widgets.notification.domain;

import com.glia.widgets.notification.device.INotificationManager;

public class ShowAudioCallNotificationUseCase {
    private final INotificationManager notificationManager;

    public ShowAudioCallNotificationUseCase(INotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void execute() {
        notificationManager.showAudioCallNotification();
    }
}
