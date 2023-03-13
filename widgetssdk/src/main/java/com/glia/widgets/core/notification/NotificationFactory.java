package com.glia.widgets.core.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.glia.widgets.R;

public class NotificationFactory {
    private static final int SCREEN_SHARING_PENDING_INTENT_REQUEST_CODE = 1;

    public static final String NOTIFICATION_SCREEN_SHARING_CHANNEL_ID = "screensharing_channel";
    public static final String NOTIFICATION_CALL_CHANNEL_ID = "call_channel";

    public static final int SCREEN_SHARING_NOTIFICATION_ID = 1;
    public static final int CALL_NOTIFICATION_ID = 2;

    public static Notification createScreenSharingNotification(Context context) {
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(
                        context,
                        SCREEN_SHARING_PENDING_INTENT_REQUEST_CODE,
                        NotificationActionReceiver.getScreenSharingEndPressedActionIntent(context),
                        PendingIntent.FLAG_IMMUTABLE
                );

        return new NotificationCompat.Builder(context, NOTIFICATION_SCREEN_SHARING_CHANNEL_ID)
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
                ).build();
    }

    public static Notification createCallStartedNotification(Context context) {
        return new NotificationCompat.Builder(context, NOTIFICATION_CALL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_mic)
                .setContentTitle(context.getString(R.string.glia_notification_audio_call_title))
                .setContentText(context.getString(R.string.glia_notification_audio_call_message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true)
                .build();
    }

    public static Notification createVideoCallStartedNotification(Context context, boolean isCallVisualizer) {
        String text = context.getString(isCallVisualizer ? R.string.glia_notification_video_call_message_no_audio : R.string.glia_notification_video_call_message);
        return new NotificationCompat.Builder(context, NOTIFICATION_CALL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_videocam)
                .setContentTitle(context.getString(R.string.glia_notification_video_call_title))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true)
                .build();
    }
}
