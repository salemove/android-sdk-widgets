package com.glia.widgets.core.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.glia.widgets.core.screensharing.ScreenSharingController;
import com.glia.widgets.di.Dependencies;

public class NotificationActionReceiver extends BroadcastReceiver {
    // NB - literals should match with ones in the manifest
    private static final String ACTION_ON_SCREEN_SHARING_END_PRESSED = "com.glia.widgets.core.notification.NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_END_PRESSED";

    public static Intent getScreenSharingEndPressedActionIntent(Context context) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(ACTION_ON_SCREEN_SHARING_END_PRESSED);
        return intent;
    }

    private final ScreenSharingController controller = Dependencies
            .getControllerFactory()
            .getScreenSharingController();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_ON_SCREEN_SHARING_END_PRESSED.equals(intent.getAction())) {
            onScreenSharingEndPressed();
        }
    }

    private void onScreenSharingEndPressed() {
        controller.onScreenSharingNotificationEndPressed();
    }
}
