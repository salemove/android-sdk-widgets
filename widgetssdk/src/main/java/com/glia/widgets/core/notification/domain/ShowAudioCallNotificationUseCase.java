package com.glia.widgets.core.notification.domain;

import com.glia.widgets.core.notification.device.INotificationManager;

public class ShowAudioCallNotificationUseCase {
    private final INotificationManager notificationManager;

    public ShowAudioCallNotificationUseCase(INotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void execute() {
        notificationManager.showAudioCallNotification();
    }
}
