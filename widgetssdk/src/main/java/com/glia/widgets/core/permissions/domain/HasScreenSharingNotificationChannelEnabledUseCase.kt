package com.glia.widgets.core.permissions.domain

import android.content.Context
import com.glia.widgets.core.notification.NotificationFactory
import com.glia.widgets.core.notification.areNotificationsEnabledForChannel

internal class HasScreenSharingNotificationChannelEnabledUseCase(private val context: Context) {
    operator fun invoke(): Boolean = context.areNotificationsEnabledForChannel(NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID)
}
