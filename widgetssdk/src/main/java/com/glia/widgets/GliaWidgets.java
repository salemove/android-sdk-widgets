package com.glia.widgets;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaConfig;
import com.glia.widgets.di.ControllerFactory;
import com.glia.widgets.di.RepositoryFactory;

public class GliaWidgets {

    public static final String UI_THEME = "ui_theme";
    public static final String COMPANY_NAME = "company_name";
    public static final String QUEUE_ID = "queue_id";
    public static final String CONTEXT_URL = "context_url";
    public static final String RETURN_DESTINATION = "return_destination";
    public static final String DESTINATION_CHAT = "destination_chat";
    public static final String DESTINATION_CALL = "destination_call";
    public static final String IS_ORIGIN_CALL = "is_origin_call";

    private static ControllerFactory controllerFactory;

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
        controllerFactory = new ControllerFactory(getRepositoryFactory());
    }

    private static RepositoryFactory getRepositoryFactory() {
        return new RepositoryFactory();
    }

    public static ControllerFactory getControllerFactory() {
        return controllerFactory;
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Glia.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
