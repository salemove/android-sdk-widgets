package com.glia.widgets.notification.device;

public interface INotificationManager {
    void showAudioCallNotification();
    void showVideoCallNotification();
    void removeCallNotification();
    void showScreenSharingNotification();
    void removeScreenSharingNotification();
}
