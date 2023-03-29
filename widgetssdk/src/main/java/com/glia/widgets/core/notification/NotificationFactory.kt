package com.glia.widgets.core.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.glia.widgets.R
import com.glia.widgets.core.notification.NotificationActionReceiver.Companion.getScreenSharingEndPressedActionIntent

internal object NotificationFactory {
    private const val SCREEN_SHARING_PENDING_INTENT_REQUEST_CODE = 1
    const val NOTIFICATION_SCREEN_SHARING_CHANNEL_ID = "screensharing_channel"
    const val NOTIFICATION_CALL_CHANNEL_ID = "call_channel"
    const val SCREEN_SHARING_NOTIFICATION_ID = 1
    const val CALL_NOTIFICATION_ID = 2

    @JvmStatic
    fun createScreenSharingNotification(context: Context): Notification {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SCREEN_SHARING_PENDING_INTENT_REQUEST_CODE,
            getScreenSharingEndPressedActionIntent(context),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, NOTIFICATION_SCREEN_SHARING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_screensharing)
            .setContentTitle(context.getString(R.string.glia_notification_screen_sharing_title))
            .setContentText(context.getString(R.string.glia_notification_screen_sharing_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_baseline_close,
                context.getString(R.string.glia_notification_action_end_sharing),
                pendingIntent
            ).build()
    }

    fun createCallStartedNotification(context: Context): Notification =
        NotificationCompat.Builder(context, NOTIFICATION_CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_mic)
            .setContentTitle(context.getString(R.string.glia_notification_audio_call_title))
            .setContentText(context.getString(R.string.glia_notification_audio_call_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .build()

    fun createVideoCallStartedNotification(
        context: Context, isCallVisualizer: Boolean
    ): Notification {
        val text =
            context.getString(if (isCallVisualizer) R.string.glia_notification_video_call_message_no_audio else R.string.glia_notification_video_call_message)

        return NotificationCompat.Builder(context, NOTIFICATION_CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_videocam)
            .setContentTitle(context.getString(R.string.glia_notification_video_call_title))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .build()
    }
}