package com.glia.widgets.core.notification.device;

import static com.glia.widgets.core.notification.NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID;
import static com.glia.widgets.core.notification.NotificationFactory.SCREEN_SHARING_NOTIFICATION_ID;

import android.app.Application;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.glia.widgets.R;
import com.glia.widgets.core.notification.NotificationFactory;
import com.glia.widgets.core.screensharing.MediaProjectionService;

public class NotificationManager implements INotificationManager {
    private final Context applicationContext;
    private final android.app.NotificationManager notificationManager;
    private final static String TAG = "NotificationManager";


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
        if (notificationManager.getNotificationChannel(NOTIFICATION_CALL_CHANNEL_ID) == null) {
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CALL_CHANNEL_ID, applicationContext.getString(R.string.glia_notification_call_channel_name), importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createScreenSharingChannel() {
        if (notificationManager.getNotificationChannel(NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID) == null) {
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID, applicationContext.getString(R.string.glia_notification_screen_sharing_channel_name), importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }

    @Override
    public void showAudioCallNotification() {
        if (areNotificationsEnabled(applicationContext, NOTIFICATION_CALL_CHANNEL_ID)) {
            notificationManager.notify(NotificationFactory.CALL_NOTIFICATION_ID, NotificationFactory.createCallStartedNotification(applicationContext));
        }
    }

    @Override
    public void removeCallNotification() {
        notificationManager.cancel(NotificationFactory.CALL_NOTIFICATION_ID);
    }

    @Override
    public void showVideoCallNotification() {
        if (areNotificationsEnabled(applicationContext, NOTIFICATION_CALL_CHANNEL_ID)) {
            notificationManager.notify(NotificationFactory.CALL_NOTIFICATION_ID, NotificationFactory.createVideoCallStartedNotification(applicationContext));
        }
    }

    /**
     * Tries showing a notification for screen sharing
     */
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

    public static boolean areNotificationsEnabled(Context context, String notificationChannelId) {
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                android.app.NotificationManager notificationManager =
                        context.getSystemService(android.app.NotificationManager.class);
                NotificationChannel channel = notificationManager.getNotificationChannel(
                        notificationChannelId
                );
                if (channel == null)
                    return true; //channel is not yet created so return boolean
                // by only checking whether notifications enabled or not
                return channel.getImportance() != android.app.NotificationManager.IMPORTANCE_NONE;
            }
            // can not check for lower level
            return true;
        }
        return false;
    }

    public static void openNotificationChannelScreen(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            context.startActivity(settingsIntent);
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        }
    }
}
