package com.glia.widgets.core.notification.device

interface INotificationManager {
    fun showAudioCallNotification()
    fun showVideoCallNotification(isTwoWayVideo: Boolean, hasAudio: Boolean)
    fun removeCallNotification()
    fun showScreenSharingNotification()
    fun removeScreenSharingNotification()
}
