package com.glia.widgets.core.notification

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

internal fun Context.openNotificationChannelScreen() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .setData(Uri.fromParts("package", packageName, null))
    }
    startActivity(intent)
}

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
