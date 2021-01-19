package com.glia.widgets;

import android.app.Application;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaConfig;
import com.glia.widgets.di.ChatControllerFactory;

public class GliaWidgets {

    public static final String UI_THEME = "ui_theme";
    public static final String COMPANY_NAME = "company_name";
    public static final String QUEUE_ID = "queue_id";
    public static final String CONTEXT_URL = "context_url";

    private static ChatControllerFactory chatControllerFactory;

    public synchronized static void onAppCreate(Application application) {
        Glia.onAppCreate(application);
    }

    public synchronized static void init(GliaWidgetsConfig gliaWidgetsConfig) {
        GliaConfig gliaConfig = new GliaConfig.Builder()
                .setApiToken(gliaWidgetsConfig.getApiToken())
                .setAppToken(gliaWidgetsConfig.getAppToken())
                .setSiteId(gliaWidgetsConfig.getSiteId())
                .setRegion(gliaWidgetsConfig.getRegion())
                .setContext(gliaWidgetsConfig.getContext())
                .build();

        Glia.init(gliaConfig);
        chatControllerFactory = new ChatControllerFactory();
    }

    public static ChatControllerFactory getChatControllerFactory() {
        return chatControllerFactory;
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Glia.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
