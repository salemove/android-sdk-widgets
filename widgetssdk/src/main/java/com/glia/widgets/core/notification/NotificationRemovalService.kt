package com.glia.widgets.core.notification

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Glia internal class.
 *
 * Will be automatically added to integrator Manifest by Manifest merger during compilation.
 *
 * This service is used to remove push notification about ongoing video/audio call when application is killed.
 * Related bug ticket: https://glia.atlassian.net/browse/MOB-2481
 */
class NotificationRemovalService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NotificationFactory.CALL_NOTIFICATION_ID)
        super.onTaskRemoved(rootIntent)
    }
}
