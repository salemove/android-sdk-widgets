package com.glia.widgets.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.screensharing.MediaProjectionService;
import com.glia.widgets.screensharing.ScreenSharingController;

import static com.glia.widgets.notification.NotificationFactory.SCREEN_SHARING_NOTIFICATION_ID;

public class NotificationActionReceiver extends BroadcastReceiver {
    // NB - literals should match with ones in the manifest
    public static final String ACTION_ON_SCREEN_SHARING_ENDED = "com.glia.widgets.notification.NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_ENDED";
    public static final String ACTION_ON_SCREEN_SHARING_START = "com.glia.widgets.notification.NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_START";
    public static final String ACTION_ON_SCREEN_SHARING_END_PRESSED = "com.glia.widgets.notification.NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_END_PRESSED";

    private final ScreenSharingController controller = GliaWidgets.getControllerFactory().getScreenSharingController(null);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_ON_SCREEN_SHARING_START.equals(intent.getAction()))
            onScreenSharingStart(context);

        if (ACTION_ON_SCREEN_SHARING_ENDED.equals(intent.getAction()))
            onScreenSharingEnded(context);

        if (ACTION_ON_SCREEN_SHARING_END_PRESSED.equals(intent.getAction())) {
            onScreenSharingEndPressed(context);
            context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
    }

    private void onScreenSharingEndPressed(Context context) {
        controller.onScreenSharingNotificationEndPressed(context);
    }

    private void onScreenSharingStart(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.startForegroundService(new Intent(context, MediaProjectionService.class));
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.notify(NotificationFactory.SCREEN_SHARING_NOTIFICATION_ID, NotificationFactory.createScreenSharingNotification(context));
        }
    }

    private void onScreenSharingEnded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.stopService(new Intent(context, MediaProjectionService.class));
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.cancel(SCREEN_SHARING_NOTIFICATION_ID);
        }
    }

    public static Intent getStartScreenSharingActionIntent(Context context) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_START);
        return intent;
    }

    public static Intent getEndScreenSharingActionIntent(Context context) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_ENDED);
        return intent;
    }

    public static Intent getScreenSharingEndPressedActionIntent(Context context) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(ACTION_ON_SCREEN_SHARING_END_PRESSED);
        return intent;
    }
}
