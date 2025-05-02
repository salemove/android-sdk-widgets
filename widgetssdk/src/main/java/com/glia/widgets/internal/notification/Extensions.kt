package com.glia.widgets.internal.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

internal fun Context.areNotificationsEnabled(): Boolean =
    NotificationManagerCompat.from(this).areNotificationsEnabled()

internal fun Context.areNotificationsEnabledForChannel(channelId: String): Boolean {
    if (areNotificationsEnabled()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getSystemService(NotificationManager::class.java)
                .getNotificationChannel(channelId)?.importance != NotificationManager.IMPORTANCE_NONE
        }
        // can not check for lower level
        return true
    }
    return false
}
