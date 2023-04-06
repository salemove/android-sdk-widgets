package com.glia.widgets.core.permissions

import android.content.Context
import android.provider.Settings
import com.glia.widgets.core.notification.NotificationFactory
import com.glia.widgets.core.notification.areNotificationsEnabledForChannel

internal class PermissionManager(private val applicationContext: Context) {

    fun hasOverlayPermission(): Boolean = Settings.canDrawOverlays(applicationContext)

    fun hasCallNotificationChannelEnabled(): Boolean =
        applicationContext.areNotificationsEnabledForChannel(NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID)

    fun hasScreenSharingNotificationChannelEnabled(): Boolean =
        applicationContext.areNotificationsEnabledForChannel(NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID)
}
