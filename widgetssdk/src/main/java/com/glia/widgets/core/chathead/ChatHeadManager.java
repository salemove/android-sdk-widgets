package com.glia.widgets.core.chathead;

import android.content.Context;
import android.content.Intent;

import com.glia.widgets.helper.Logger;

/**
 * @hide
 */
public class ChatHeadManager {
    private static final String TAG = ChatHeadManager.class.getSimpleName();

    private final Context applicationContext;
    private final Intent chatHeadServiceIntent;

    private boolean isServiceStarted = false;

    public ChatHeadManager(
            Context applicationContext
    ) {
        this.applicationContext = applicationContext;
        this.chatHeadServiceIntent = ChatHeadService.getIntent(applicationContext);
    }

    public void startChatHeadService() {
        if (!isServiceStarted) {
            isServiceStarted = true;
            try {
                applicationContext.startService(chatHeadServiceIntent);
            } catch (IllegalStateException exception) {
                isServiceStarted = false;
                Logger.w(TAG, "Application is in a state where the service can not be started" +
                    " (such as not in the foreground in a state when services are allowed)");
                // This prevents the 'Not allowed to start ChatHeadService: app is in background' crash.
                // Example scenario:
                // Disable the 'Display over other apps' Android setting for Widgets example app
                // Open Widgets example app, tap 'Start new chat flow'
                // Tap 'OK' for 'Screen Overlay Permissions Required' dialog, you will be navigated to Android Settings
                // Open Widgets example app, go to the main screen to have chat bubble displayed
                // Come back to Android Settings, allow the 'Display over other apps' setting
                // Tap device Home button (bubble is not displayed)
                // Go to Glia Hub, wait for 60+ seconds
                // Accept engagement
                // IllegalStateException will be caught. Once visitor opens Widgets example app, bubble will be displayed and start working.
            }
        }
    }

    public void stopChatHeadService() {
        if (isServiceStarted) {
            isServiceStarted = false;
            applicationContext.stopService(chatHeadServiceIntent);
        }
    }
}
