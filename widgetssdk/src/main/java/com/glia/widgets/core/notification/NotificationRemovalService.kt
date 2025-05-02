package com.glia.widgets.core.notification

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.glia.widgets.internal.notification.NotificationFactory

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * This service is used to remove push notifications about ongoing video/audio calls when the application is killed.
 */
internal class NotificationRemovalService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NotificationFactory.CALL_NOTIFICATION_ID)
        super.onTaskRemoved(rootIntent)
    }
}
