package com.glia.widgets.internal.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies

internal object NotificationFactory {
    const val NOTIFICATION_CALL_CHANNEL_ID = "call_channel"
    const val CALL_NOTIFICATION_ID = 2

    // We're using the same ID for secure messaging notifications, because we have the same hardcoded text for all the notifications,
    // and there is no need to show 10 notifications with the same text.
    const val SECURE_MESSAGING_NOTIFICATION_ID = 3
    const val NOTIFICATION_SECURE_MESSAGING_CHANNEL_ID = "secure_messaging_channel"

    private val localeProvider by lazy { Dependencies.localeProvider }

    fun createAudioCallNotification(context: Context): Notification =
        createCallNotification(
            context = context,
            icon = R.drawable.ic_baseline_mic,
            title = localeProvider.getString(R.string.android_notification_audio_call_title),
            message = localeProvider.getString(R.string.android_notification_audio_call_message)
        )

    fun createVideoCallNotification(
        context: Context,
        isTwoWayVideo: Boolean,
        hasAudio: Boolean
    ): Notification {
        return if (isTwoWayVideo) {
            createTwoWayVideoNotification(context, hasAudio)
        } else {
            createOneWayVideoNotification(context, hasAudio)
        }
    }

    private fun createOneWayVideoNotification(context: Context, hasAudio: Boolean): Notification {
        val message =
            if (hasAudio) {
                localeProvider.getString(R.string.android_notification_one_way_video_message)
            } else {
                localeProvider.getString(R.string.android_notification_one_way_video_no_audio_message)
            }
        return createCallNotification(
            context = context,
            icon = R.drawable.ic_baseline_videocam,
            title = localeProvider.getString(R.string.android_notification_one_way_video_title),
            message = message
        )
    }

    private fun createTwoWayVideoNotification(context: Context, hasAudio: Boolean): Notification {
        val message =
            if (hasAudio) {
                localeProvider.getString(R.string.android_notification_two_way_video_message)
            } else {
                localeProvider.getString(R.string.android_notification_two_way_video_no_audio_message)
            }
        return createCallNotification(
            context = context,
            icon = R.drawable.ic_baseline_videocam,
            title = localeProvider.getString(R.string.android_notification_two_way_video_title),
            message = message
        )
    }

    private fun createCallNotification(
        context: Context,
        @DrawableRes icon: Int,
        title: String,
        message: String
    ): Notification {
        return NotificationCompat.Builder(context, NOTIFICATION_CALL_CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            //Based on priority level description, audio/video notification will always be below screen-sharing notification in a list
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setSilent(true) // No sound or vibration and no heads-up notification
            .build()
    }

    fun createSecureMessagingNotification(context: Context, content: String, contentIntent: PendingIntent): Notification =
        NotificationCompat.Builder(context, NOTIFICATION_SECURE_MESSAGING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chat)
            .setContentIntent(contentIntent)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .build()
}
