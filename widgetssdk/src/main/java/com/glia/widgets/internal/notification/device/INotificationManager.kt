package com.glia.widgets.internal.notification.device

import android.app.PendingIntent
import com.glia.widgets.internal.notification.domain.CallNotification

internal interface INotificationManager {
    // Returns false when the system refused the start.
    fun startCallService(call: CallNotification, usesMicrophone: Boolean, usesCamera: Boolean): Boolean

    // Posts or updates the call notification. It also updates the text of a running call service.
    fun showCallNotification(call: CallNotification)
    fun removeCallNotification()
    fun startNotificationRemovalService()
    fun showSecureMessageNotification(content: String, contentIntent: PendingIntent)
    fun createSecureMessagingChannel()
    fun showInteractionNotification(content: String, contentIntent: PendingIntent)
    fun createInteractionChannel()
}
