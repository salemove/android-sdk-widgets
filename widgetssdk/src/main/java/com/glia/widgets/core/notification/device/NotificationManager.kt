package com.glia.widgets.core.notification.device

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.glia.widgets.R
import com.glia.widgets.core.notification.NotificationFactory
import com.glia.widgets.core.notification.areNotificationsEnabledForChannel
import com.glia.widgets.core.screensharing.MediaProjectionService

internal class NotificationManager(private val applicationContext: Application) :
    INotificationManager {
    private val notificationManager: NotificationManager by lazy {
        applicationContext.getSystemService(NotificationManager::class.java)
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createCallChannel()
            createScreenSharingChannel()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createCallChannel() {
        if (notificationManager.getNotificationChannel(NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID) == null) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID,
                applicationContext.getString(R.string.glia_notification_call_channel_name),
                importance
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createScreenSharingChannel() {
        if (notificationManager.getNotificationChannel(NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID) == null) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID,
                applicationContext.getString(R.string.glia_notification_screen_sharing_channel_name),
                importance
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun showAudioCallNotification() {
        if (areNotificationsEnabledForChannel(NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID)) {
            notificationManager.notify(
                NotificationFactory.CALL_NOTIFICATION_ID,
                NotificationFactory.createAudioCallNotification(applicationContext)
            )
        }
    }

    override fun showVideoCallNotification(isTwoWayVideo: Boolean, hasAudio: Boolean) {
        if (areNotificationsEnabledForChannel(NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID)) {
            notificationManager.notify(
                NotificationFactory.CALL_NOTIFICATION_ID,
                NotificationFactory.createVideoCallNotification(applicationContext, isTwoWayVideo, hasAudio)
            )
        }
    }

    override fun removeCallNotification() {
        notificationManager.cancel(NotificationFactory.CALL_NOTIFICATION_ID)
    }

    /**
     * Tries showing a notification for screen sharing
     */
    override fun showScreenSharingNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(
                Intent(applicationContext, MediaProjectionService::class.java)
            )
        } else {
            notificationManager.notify(
                NotificationFactory.SCREEN_SHARING_NOTIFICATION_ID,
                NotificationFactory.createScreenSharingNotification(applicationContext)
            )
        }
    }

    override fun removeScreenSharingNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.stopService(
                Intent(
                    applicationContext,
                    MediaProjectionService::class.java
                )
            )
        } else {
            notificationManager.cancel(NotificationFactory.SCREEN_SHARING_NOTIFICATION_ID)
        }
    }

    private fun areNotificationsEnabledForChannel(id: String) =
        applicationContext.areNotificationsEnabledForChannel(id)
}
