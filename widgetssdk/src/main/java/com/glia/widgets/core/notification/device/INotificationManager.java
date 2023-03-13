package com.glia.widgets.core.notification.device;

public interface INotificationManager {
    void showAudioCallNotification();

    void showVideoCallNotification(boolean isCallVisualizer);

    void removeCallNotification();

    void showScreenSharingNotification();

    void removeScreenSharingNotification();
}
