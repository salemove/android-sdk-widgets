package com.glia.widgets.core.chathead;

import android.content.Context;
import android.content.Intent;


public class ChatHeadManager {
    private final Context applicationContext;
    private final Intent chatHeadServiceIntent;

    private boolean isServiceStarted = false;

    public ChatHeadManager(
            Context applicationContext
    ) {
        this.applicationContext = applicationContext;
        this.chatHeadServiceIntent = ChatHeadService.getIntent(applicationContext);
    }

    public boolean isServiceStarted() {
        return isServiceStarted;
    }

    // Service start
    public void startChatHeadService() {
        if (!isServiceStarted) {
            isServiceStarted = true;
            applicationContext.startService(chatHeadServiceIntent);
        }
    }

    // Service stop
    public void stopChatHeadService() {
        if (isServiceStarted) {
            isServiceStarted = false;
            applicationContext.stopService(chatHeadServiceIntent);
        }
    }
}
