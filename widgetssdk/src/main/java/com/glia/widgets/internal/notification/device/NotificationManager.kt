package com.glia.widgets.internal.notification.device

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.StringRes
import com.glia.widgets.R
import com.glia.widgets.core.notification.NotificationRemovalService
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.internal.notification.NotificationFactory
import com.glia.widgets.internal.notification.areNotificationsEnabledForChannel

internal class NotificationManager(
    private val applicationContext: Application,
) : INotificationManager {
    private val notificationManager: NotificationManager by lazy {
        applicationContext.getSystemService(NotificationManager::class.java)
    }

    val notificationRemovalServiceIntent: Intent by lazy {
        Intent(applicationContext, NotificationRemovalService::class.java)
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        createCallChannel()
        createScreenSharingChannel()
    }

    private fun createCallChannel() = createChannelIfDoesNotExist(
        NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID,
        R.string.android_notification_audio_call_channel_name,
        /* Call notification importance should have the possible highest value,
        because it shows the ongoing audio/video streaming and it is preferable to be as high as possible in notifications list */
        NotificationManager.IMPORTANCE_HIGH
    )

    private fun createScreenSharingChannel() = createChannelIfDoesNotExist(
        NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID,
        R.string.android_notification_screen_sharing_channel_name,
        /* Screen sharing notification importance should have the possible highest value,
            because it affects the Media projection service initialization and streaming timing */
        NotificationManager.IMPORTANCE_HIGH
    )

    override fun createSecureMessagingChannel() = createChannelIfDoesNotExist(
        NotificationFactory.NOTIFICATION_SECURE_MESSAGING_CHANNEL_ID,
        R.string.android_notification_secure_messaging_channel_name,
        NotificationManager.IMPORTANCE_HIGH
    )

    private fun createChannelIfDoesNotExist(id: String, @StringRes nameRes: Int, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(id) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(id, applicationContext.getString(nameRes), importance)
            )
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
            applicationContext.startService(notificationRemovalServiceIntent)
        } catch (error: Throwable) {
            // Above code is known to throw an error if app is in background
            Logger.e(TAG, "Failed to launch 'NotificationRemovalService'", error)
        }
    }

    override fun removeCallNotification() {
        notificationManager.cancel(NotificationFactory.CALL_NOTIFICATION_ID)
        applicationContext.stopService(notificationRemovalServiceIntent)
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

    override fun showSecureMessageNotification(content: String, contentIntent: PendingIntent) {
        if (areNotificationsEnabledForChannel(NotificationFactory.NOTIFICATION_SECURE_MESSAGING_CHANNEL_ID)) {
            notificationManager.notify(
                NotificationFactory.SECURE_MESSAGING_NOTIFICATION_ID,
                NotificationFactory.createSecureMessagingNotification(
                    applicationContext,
                    content,
                    contentIntent
                )
            )
        }
    }

    private fun areNotificationsEnabledForChannel(id: String) = applicationContext.areNotificationsEnabledForChannel(id)
}
