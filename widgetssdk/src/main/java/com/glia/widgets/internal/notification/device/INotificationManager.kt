package com.glia.widgets.internal.notification.device

import android.app.PendingIntent

internal interface INotificationManager {
    fun showAudioCallNotification()
    fun showVideoCallNotification(isTwoWayVideo: Boolean, hasAudio: Boolean)
    fun removeCallNotification()
    fun startNotificationRemovalService()
    fun showSecureMessageNotification(content: String, contentIntent: PendingIntent)
    fun createSecureMessagingChannel()
}
