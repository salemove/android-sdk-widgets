package com.glia.exampleapp;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.glia.widgets.GliaWidgetsConfig;
import com.glia.widgets.GliaWidgets;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GliaWidgets.onAppCreate(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String appToken = sharedPreferences.getString(getString(R.string.pref_app_token), getString(R.string.app_token));
        String apiToken = sharedPreferences.getString(getString(R.string.pref_api_token), getString(R.string.api_token));
        String siteId = sharedPreferences.getString(getString(R.string.pref_site_id), getString(R.string.site_id));
        GliaWidgetsConfig gliaConfig = new GliaWidgetsConfig.Builder()
                .setApiToken(apiToken)
                .setAppToken(appToken)
                .setSiteId(siteId)
                .setRegion("beta")
                .setContext(getApplicationContext())
                .build();

        GliaWidgets.init(gliaConfig);
    }
}
