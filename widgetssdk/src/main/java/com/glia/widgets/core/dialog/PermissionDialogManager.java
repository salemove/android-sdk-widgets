package com.glia.widgets.core.dialog;

import android.content.Context;
import android.content.SharedPreferences;

public class PermissionDialogManager {
    private static final String TAG = PermissionDialogManager.class.getSimpleName();
    private static final String PREF_KEY = "dialog_permission_manager";

    private static final String OVERLAY_PERMISSION_DIALOG_SHOWN_KEY = "OVERLAY_PERMISSION_DIALOG_SHOWN_KEY";
    private static final String CALL_CHANNEL_NOTIFICATION_DIALOG_SHOWN_KEY = "CALL_CHANNEL_NOTIFICATION_DIALOG_SHOWN_KEY";

    private final SharedPreferences sharedPreferences;

    public PermissionDialogManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    }

    public void resetPermissionDialogs() {
        sharedPreferences.edit().clear().apply();
    }

    public void setOverlayPermissionDialogShown() {
        sharedPreferences.edit().putBoolean(OVERLAY_PERMISSION_DIALOG_SHOWN_KEY, true).apply();
    }

    public boolean hasOverlayPermissionDialogShown() {
        return sharedPreferences.getBoolean(OVERLAY_PERMISSION_DIALOG_SHOWN_KEY, false);
    }

    public void setEnableCallNotificationChannelRequestShown() {
        sharedPreferences.edit().putBoolean(CALL_CHANNEL_NOTIFICATION_DIALOG_SHOWN_KEY, true).apply();
    }

    public boolean hasEnableCallNotificationChannelRequestShown() {
        return sharedPreferences.getBoolean(CALL_CHANNEL_NOTIFICATION_DIALOG_SHOWN_KEY, false);
    }
}
