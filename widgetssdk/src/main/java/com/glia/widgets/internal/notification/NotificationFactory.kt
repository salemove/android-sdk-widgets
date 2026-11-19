package com.glia.widgets.internal.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.glia.androidsdk.comms.MediaDirection
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.internal.notification.domain.CallNotification
import com.glia.widgets.internal.notification.domain.CallNotification.Phase

internal object NotificationFactory {
    const val NOTIFICATION_CALL_CHANNEL_ID = "call_channel"
    const val CALL_NOTIFICATION_ID = 2

    // We're using the same ID for secure messaging notifications, because we have the same hardcoded text for all the notifications,
    // and there is no need to show 10 notifications with the same text.
    const val SECURE_MESSAGING_NOTIFICATION_ID = 3
    const val NOTIFICATION_SECURE_MESSAGING_CHANNEL_ID = "secure_messaging_channel"

    // One ID for interaction push notifications too, so a newer push replaces the older one.
    const val INTERACTION_NOTIFICATION_ID = 4
    const val NOTIFICATION_INTERACTION_CHANNEL_ID = "interaction_channel"

    private val localeProvider by lazy { Dependencies.localeProvider }

    fun createCallNotification(context: Context, call: CallNotification): Notification {
        val isVideo = call.video != MediaDirection.NONE
        val (title, message) = when (call.phase) {
            Phase.WAITING -> callTitle(isVideo) to R.string.android_call_queue_message
            Phase.CONNECTING -> callTitle(isVideo) to R.string.glia_notification_call_connecting_message
            Phase.LIVE -> liveCallTexts(call)
        }
        return createCallNotification(
            context = context,
            icon = if (isVideo) R.drawable.ic_baseline_videocam else R.drawable.ic_baseline_mic,
            title = localeProvider.getString(title),
            message = localeProvider.getString(message)
        )
    }

    private fun callTitle(isVideo: Boolean): Int = if (isVideo) R.string.engagement_video_title else R.string.engagement_audio_title

    private fun liveCallTexts(call: CallNotification): Pair<Int, Int> = when (call.video) {
        MediaDirection.TWO_WAY -> R.string.android_notification_two_way_video_title to if (call.hasAudio) {
            R.string.android_notification_two_way_video_message
        } else {
            R.string.android_notification_two_way_video_no_audio_message
        }

        MediaDirection.ONE_WAY -> R.string.android_notification_one_way_video_title to if (call.hasAudio) {
            R.string.android_notification_one_way_video_message
        } else {
            R.string.android_notification_one_way_video_no_audio_message
        }

        else -> R.string.android_notification_audio_call_title to R.string.android_notification_audio_call_message
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
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setSilent(true) // No sound or vibration and no heads-up notification
            .build()
    }

    fun createSecureMessagingNotification(context: Context, content: String, contentIntent: PendingIntent): Notification =
        createMessageNotification(context, NOTIFICATION_SECURE_MESSAGING_CHANNEL_ID, content, contentIntent)

    fun createInteractionNotification(context: Context, content: String, contentIntent: PendingIntent): Notification =
        createMessageNotification(context, NOTIFICATION_INTERACTION_CHANNEL_ID, content, contentIntent)

    private fun createMessageNotification(context: Context, channelId: String, content: String, contentIntent: PendingIntent): Notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_chat)
            .setContentIntent(contentIntent)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .build()
}
