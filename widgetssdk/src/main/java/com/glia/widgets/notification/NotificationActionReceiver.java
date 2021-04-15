package com.glia.widgets.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.glia.widgets.di.Dependencies;
import com.glia.widgets.screensharing.ScreenSharingController;

public class NotificationActionReceiver extends BroadcastReceiver {
    // NB - literals should match with ones in the manifest
    public static final String ACTION_ON_SCREEN_SHARING_END_PRESSED = "com.glia.widgets.notification.NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_END_PRESSED";

    private final ScreenSharingController controller = Dependencies.getControllerFactory().getScreenSharingController(null);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_ON_SCREEN_SHARING_END_PRESSED.equals(intent.getAction())) {
            onScreenSharingEndPressed(context);
            context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
    }

    private void onScreenSharingEndPressed(Context context) {
        controller.onScreenSharingNotificationEndPressed(context);
    }

    public static Intent getScreenSharingEndPressedActionIntent(Context context) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(ACTION_ON_SCREEN_SHARING_END_PRESSED);
        return intent;
    }
}
