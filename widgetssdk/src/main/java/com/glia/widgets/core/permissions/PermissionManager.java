package com.glia.widgets.core.permissions;

import android.content.Context;
import android.provider.Settings;

import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.notification.NotificationFactory;
import com.glia.widgets.core.notification.device.NotificationManager;

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
