package com.glia.widgets.internal.notification.device

internal interface INotificationManager {
    fun showAudioCallNotification()
    fun showVideoCallNotification(isTwoWayVideo: Boolean, hasAudio: Boolean)
    fun removeCallNotification()
    fun showScreenSharingNotification()
    fun removeScreenSharingNotification()
    fun startNotificationRemovalService()
}
