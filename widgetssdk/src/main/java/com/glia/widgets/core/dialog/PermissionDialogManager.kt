package com.glia.widgets.core.dialog

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val PREF_KEY = "dialog_permission_manager"
private const val OVERLAY_PERMISSION_DIALOG_SHOWN_KEY = "OVERLAY_PERMISSION_DIALOG_SHOWN_KEY"
private const val CALL_CHANNEL_NOTIFICATION_DIALOG_SHOWN_KEY = "CALL_CHANNEL_NOTIFICATION_DIALOG_SHOWN_KEY"

internal class PermissionDialogManager(context: Context) {
    private val sharedPreferences: SharedPreferences by lazy { context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE) }

    fun setOverlayPermissionDialogShown() {
        sharedPreferences.edit { putBoolean(OVERLAY_PERMISSION_DIALOG_SHOWN_KEY, true) }
    }

    fun hasOverlayPermissionDialogShown(): Boolean = sharedPreferences.getBoolean(OVERLAY_PERMISSION_DIALOG_SHOWN_KEY, false)

    fun setEnableCallNotificationChannelRequestShown() {
        sharedPreferences.edit { putBoolean(CALL_CHANNEL_NOTIFICATION_DIALOG_SHOWN_KEY, true) }
    }

    fun hasEnableCallNotificationChannelRequestShown(): Boolean = sharedPreferences.getBoolean(CALL_CHANNEL_NOTIFICATION_DIALOG_SHOWN_KEY, false)

}
