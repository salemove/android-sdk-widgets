package com.glia.exampleapp;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.GliaWidgetsConfig;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GliaWidgets.onAppCreate(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String appToken = sharedPreferences.getString(getString(R.string.pref_app_token), getString(R.string.app_token));
        String siteId = sharedPreferences.getString(getString(R.string.pref_site_id), getString(R.string.site_id));
        GliaWidgetsConfig gliaConfig = new GliaWidgetsConfig.Builder()
                .setAppToken(appToken)
                .setSiteId(siteId)
                .setRegion("beta")
                .setContext(getApplicationContext())
                .build();

        GliaWidgets.init(gliaConfig);
    }
}
