package com.glia.widgets.core.notification

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

class NotificationRemovalService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NotificationFactory.CALL_NOTIFICATION_ID)
        super.onTaskRemoved(rootIntent)
    }
}
