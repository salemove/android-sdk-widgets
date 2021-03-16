package com.glia.widgets.notification.device;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.glia.widgets.R;
import com.glia.widgets.notification.NotificationFactory;
import com.glia.widgets.screensharing.MediaProjectionService;

import static com.glia.widgets.notification.NotificationFactory.SCREEN_SHARING_NOTIFICATION_ID;

public class NotificationManager implements INotificationManager {
    private final Context applicationContext;
    private final android.app.NotificationManager notificationManager;

    private Notification callNotification;

    public NotificationManager(Application application) {
        this.applicationContext = application;
        this.notificationManager = application.getSystemService(android.app.NotificationManager.class);

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createCallChannel();
            createScreenSharingChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createCallChannel() {
        if (notificationManager.getNotificationChannel(NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID) == null) {
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID, applicationContext.getString(R.string.notification_call_channel_name), importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createScreenSharingChannel() {
        if (notificationManager.getNotificationChannel(NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID) == null) {
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID, applicationContext.getString(R.string.notification_screen_sharing_channel_name), importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }

    @Override
    public void showAudioCallNotification() {
        callNotification = NotificationFactory.createCallStartedNotification(applicationContext);
        notificationManager.notify(NotificationFactory.CALL_NOTIFICATION_ID, callNotification);
    }

    @Override
    public void removeCallNotification() {
        if (callNotification != null) {
            notificationManager.cancel(NotificationFactory.CALL_NOTIFICATION_ID);
        }
    }

    @Override
    public void showVideoCallNotification() {
        callNotification = NotificationFactory.createVideoCallStartedNotification(applicationContext);
        notificationManager.notify(NotificationFactory.CALL_NOTIFICATION_ID, callNotification);
    }

    @Override
    public void showScreenSharingNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(new Intent(applicationContext, MediaProjectionService.class));
        } else {
            notificationManager.notify(NotificationFactory.SCREEN_SHARING_NOTIFICATION_ID, NotificationFactory.createScreenSharingNotification(applicationContext));
        }
    }

    @Override
    public void removeScreenSharingNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.stopService(new Intent(applicationContext, MediaProjectionService.class));
        } else {
            notificationManager.cancel(SCREEN_SHARING_NOTIFICATION_ID);
        }
    }
}
