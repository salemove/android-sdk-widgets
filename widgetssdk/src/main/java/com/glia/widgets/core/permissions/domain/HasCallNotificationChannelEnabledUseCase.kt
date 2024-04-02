package com.glia.widgets.core.permissions.domain

import android.content.Context
import com.glia.widgets.core.notification.NotificationFactory
import com.glia.widgets.core.notification.areNotificationsEnabledForChannel

internal class HasCallNotificationChannelEnabledUseCase(private val context: Context) {
    operator fun invoke(): Boolean = context.areNotificationsEnabledForChannel(NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID)
}
