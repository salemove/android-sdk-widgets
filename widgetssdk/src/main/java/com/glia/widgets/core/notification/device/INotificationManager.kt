package com.glia.widgets.core.notification.device

interface INotificationManager {
    fun showAudioCallNotification()
    fun showVideoCallNotification(isCallVisualizer: Boolean)
    fun removeCallNotification()
    fun showScreenSharingNotification()
    fun removeScreenSharingNotification()
}