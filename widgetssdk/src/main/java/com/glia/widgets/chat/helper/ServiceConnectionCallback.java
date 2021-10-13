package com.glia.widgets.chat.helper;

import androidx.browser.customtabs.CustomTabsClient;

public interface ServiceConnectionCallback {
    void onServiceConnected(CustomTabsClient client);

    void onServiceDisconnected();
}
