package com.glia.widgets.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.glia.widgets.R;

public class NotificationFactory {
    private static final int SCREEN_SHARING_PENDING_INTENT_REQUEST_CODE = 1;

    public static final String NOTIFICATION_SCREEN_SHARING_CHANNEL_ID = "screensharing_channel";

    public static final int SCREEN_SHARING_NOTIFICATION_ID = 1;

    public static Notification createScreenSharingNotification(Context context) {
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(
                        context,
                        SCREEN_SHARING_PENDING_INTENT_REQUEST_CODE,
                        NotificationActionReceiver.getScreenSharingEndPressedActionIntent(context),
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        return new NotificationCompat.Builder(context, NOTIFICATION_SCREEN_SHARING_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_screensharing)
                .setContentTitle(context.getString(R.string.notification_screen_sharing_title))
                .setContentText(context.getString(R.string.notification_screen_sharing_message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .addAction(
                        R.drawable.ic_baseline_close,
                        context.getString(R.string.notification_action_end_sharing),
                        pendingIntent
                ).build();
    }

}
