package com.glia.widgets.core.notification.device

internal interface INotificationManager {
    fun showAudioCallNotification()
    fun showVideoCallNotification(isTwoWayVideo: Boolean, hasAudio: Boolean)
    fun removeCallNotification()
    fun showScreenSharingNotification()
    fun removeScreenSharingNotification()
    fun startNotificationRemovalService()
}
