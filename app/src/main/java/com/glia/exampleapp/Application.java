package com.glia.exampleapp;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaConfig;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Glia.onAppCreate(this);

        GliaConfig gliaConfig = new GliaConfig.Builder()
                .setApiToken(getString(R.string.api_token))
                .setAppToken(getString(R.string.app_token))
                .setSiteId(getString(R.string.site_id))
                .setRegion(GliaConfig.Regions.EU)
                .setContext(getApplicationContext())
                .build();

        Glia.init(gliaConfig);
    }
}
