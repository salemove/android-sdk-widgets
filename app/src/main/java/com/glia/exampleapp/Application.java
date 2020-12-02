package com.glia.exampleapp;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaConfig;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Glia.onAppCreate(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String appToken = sharedPreferences.getString(getString(R.string.pref_app_token), getString(R.string.app_token));
        String apiToken = sharedPreferences.getString(getString(R.string.pref_api_token), getString(R.string.api_token));
        String siteId = sharedPreferences.getString(getString(R.string.pref_site_id), getString(R.string.site_id));
        GliaConfig gliaConfig = new GliaConfig.Builder()
                .setApiToken(apiToken)
                .setAppToken(appToken)
                .setSiteId(siteId)
                .setRegion(GliaConfig.Regions.EU)
                .setContext(getApplicationContext())
                .build();

        Glia.init(gliaConfig);
    }
}
