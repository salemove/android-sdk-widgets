package com.glia.widgets.internal.notification.device

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.glia.widgets.R
import com.glia.widgets.internal.notification.NotificationFactory
import com.glia.widgets.core.notification.NotificationRemovalService
import com.glia.widgets.internal.notification.areNotificationsEnabledForChannel
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class NotificationManager(private val applicationContext: Application) : INotificationManager {
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
            /* Screen sharing notification importance should have the possible highest value,
            because it shows the ongoing audio/video streaming and it is preferable to be as high as possible in notifications list */
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID,
                applicationContext.getString(R.string.android_notification_audio_call_channel_name),
                importance
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createScreenSharingChannel() {
        if (notificationManager.getNotificationChannel(NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID) == null) {
            /* Screen sharing notification importance should have the possible highest value,
            because it affects the Media projection service initialization and streaming timing */
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID,
                applicationContext.getString(R.string.android_notification_screen_sharing_channel_name),
                importance
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun showAudioCallNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android APIs 31+ have built in privacy indicators for microphone and camera
            // See https://source.android.com/docs/core/permissions/privacy-indicators
            return
        }
        if (areNotificationsEnabledForChannel(NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID)) {
            notificationManager.notify(
                NotificationFactory.CALL_NOTIFICATION_ID,
                NotificationFactory.createAudioCallNotification(applicationContext)
            )
        }
    }

    override fun showVideoCallNotification(isTwoWayVideo: Boolean, hasAudio: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android APIs 31+ have built in privacy indicators for microphone and camera
            // See https://source.android.com/docs/core/permissions/privacy-indicators
            return
        }
        if (areNotificationsEnabledForChannel(NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID)) {
            notificationManager.notify(
                NotificationFactory.CALL_NOTIFICATION_ID,
                NotificationFactory.createVideoCallNotification(applicationContext, isTwoWayVideo, hasAudio)
            )
        }
    }

    override fun startNotificationRemovalService() {
        // If this service is not already running, it will be instantiated and started (creating a process for it if needed);
        // if it is running then it remains running.
        try {
            applicationContext.startService(Intent(applicationContext, NotificationRemovalService::class.java))
        } catch (error: Throwable) {
            // Above code is known to throw an error if app is in background
            Logger.e(TAG, "Failed to launch 'NotificationRemovalService'", error)
        }
    }

    override fun removeCallNotification() {
        notificationManager.cancel(NotificationFactory.CALL_NOTIFICATION_ID)
        applicationContext.stopService(Intent(applicationContext, NotificationRemovalService::class.java))
    }

    /**
     * Displays notification informing the user that screen sharing is active.
     */
    override fun showScreenSharingNotification() = notificationManager.notify(
        NotificationFactory.SCREEN_SHARING_NOTIFICATION_ID,
        NotificationFactory.createScreenSharingNotification(applicationContext)
    )

    /**
     * Removes the screen sharing notification.
     */
    override fun removeScreenSharingNotification() = notificationManager.cancel(NotificationFactory.SCREEN_SHARING_NOTIFICATION_ID)

    private fun areNotificationsEnabledForChannel(id: String) = applicationContext.areNotificationsEnabledForChannel(id)
}
