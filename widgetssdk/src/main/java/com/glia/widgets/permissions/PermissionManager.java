package com.glia.widgets.permissions;

import android.content.Context;
import android.provider.Settings;

import com.glia.widgets.dialog.PermissionDialogManager;
import com.glia.widgets.notification.NotificationFactory;
import com.glia.widgets.notification.device.NotificationManager;

public class PermissionManager {
    private static final String TAG = PermissionDialogManager.class.getSimpleName();

    private final Context applicationContext;

    public PermissionManager(Context context) {
        this.applicationContext = context;
    }

    public boolean hasOverlayPermission() {
        return Settings.canDrawOverlays(applicationContext);
    }

    public boolean hasCallNotificationChannelEnabled() {
        return NotificationManager.areNotificationsEnabled(applicationContext, NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID);
    }

    public boolean hasScreenSharingNotificationChannelEnabled() {
        return NotificationManager.areNotificationsEnabled(applicationContext, NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID);
    }
}
