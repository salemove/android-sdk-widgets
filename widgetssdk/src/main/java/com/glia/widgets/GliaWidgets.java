package com.glia.widgets;

import android.app.Application;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaConfig;

public class GliaWidgets {

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
    }
}
