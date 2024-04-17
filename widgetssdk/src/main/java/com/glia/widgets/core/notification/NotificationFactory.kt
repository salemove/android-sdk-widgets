package com.glia.widgets.core.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.glia.widgets.R
import com.glia.widgets.core.notification.NotificationActionReceiver.Companion.getScreenSharingEndPressedActionIntent
import com.glia.widgets.di.Dependencies

internal object NotificationFactory {
    private const val SCREEN_SHARING_PENDING_INTENT_REQUEST_CODE = 1
    const val NOTIFICATION_SCREEN_SHARING_CHANNEL_ID = "screensharing_channel"
    const val NOTIFICATION_CALL_CHANNEL_ID = "call_channel"
    const val SCREEN_SHARING_NOTIFICATION_ID = 1
    const val CALL_NOTIFICATION_ID = 2
    private val stringProvider by lazy { Dependencies.getStringProvider() }

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
            .setContentTitle(stringProvider.getRemoteString(R.string.android_notification_screen_sharing_title))
            .setContentText(stringProvider.getRemoteString(R.string.android_notification_screen_sharing_message))
            //Screen-sharing notification should be the highest in the app notifications list, because it contains action item
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_baseline_close,
                stringProvider.getRemoteString(R.string.android_notification_end_screen_sharing_title),
                pendingIntent
            ).build()
    }

    fun createAudioCallNotification(context: Context): Notification =
        createCallNotification(
            context = context,
            icon = R.drawable.ic_baseline_mic,
            title = stringProvider.getRemoteString(R.string.android_notification_audio_call_title),
            message = stringProvider.getRemoteString(R.string.android_notification_audio_call_message)
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
                stringProvider.getRemoteString(R.string.android_notification_one_way_video_message)
            } else {
                stringProvider.getRemoteString(R.string.android_notification_one_way_video_no_audio_message)
            }
        return createCallNotification(
            context = context,
            icon = R.drawable.ic_baseline_videocam,
            title = stringProvider.getRemoteString(R.string.android_notification_one_way_video_title),
            message = message
        )
    }

    private fun createTwoWayVideoNotification(context: Context, hasAudio: Boolean): Notification {
        val message =
            if (hasAudio) {
                stringProvider.getRemoteString(R.string.android_notification_two_way_video_message)
            } else {
                stringProvider.getRemoteString(R.string.android_notification_two_way_video_no_audio_message)
            }
        return createCallNotification(
            context = context,
            icon = R.drawable.ic_baseline_videocam,
            title = stringProvider.getRemoteString(R.string.android_notification_two_way_video_title),
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
            .build()
    }
}
