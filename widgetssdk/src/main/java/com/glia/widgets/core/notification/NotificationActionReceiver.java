package com.glia.widgets.core.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.glia.widgets.di.Dependencies;
import com.glia.widgets.core.screensharing.ScreenSharingController;

public class NotificationActionReceiver extends BroadcastReceiver {
    // NB - literals should match with ones in the manifest
    public static final String ACTION_ON_SCREEN_SHARING_END_PRESSED = "com.glia.widgets.core.notification.NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_END_PRESSED";

    private final ScreenSharingController controller = Dependencies.getControllerFactory().getScreenSharingController(null);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_ON_SCREEN_SHARING_END_PRESSED.equals(intent.getAction())) {
            onScreenSharingEndPressed();
            context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
    }

    private void onScreenSharingEndPressed() {
        controller.onScreenSharingNotificationEndPressed();
    }

    public static Intent getScreenSharingEndPressedActionIntent(Context context) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(ACTION_ON_SCREEN_SHARING_END_PRESSED);
        return intent;
    }
}
