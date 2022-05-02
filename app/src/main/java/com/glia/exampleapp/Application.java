package com.glia.exampleapp;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.glia.androidsdk.SiteApiKey;
import com.glia.exampleapp.auth.AuthorizationType;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.GliaWidgetsConfig;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GliaWidgets.onAppCreate(this);
        GliaWidgets.init(createGliaAuthenticationConfiguration());
    }

    private GliaWidgetsConfig createGliaAuthenticationConfiguration() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int authorizationType = sharedPreferences.getInt(getString(R.string.pref_authorization_type), AuthorizationType.DEFAULT);
        if (authorizationType == AuthorizationType.SITE_API_KEY)
            return configWithSiteApiKeyAuth(sharedPreferences);
        else if (authorizationType == AuthorizationType.APP_TOKEN)
            return configWithAppTokenAuth(sharedPreferences);
        else return null;
    }

    private GliaWidgetsConfig configWithAppTokenAuth(SharedPreferences preferences) {
        String appToken = preferences.getString(getString(R.string.pref_app_token), getString(R.string.app_token));
        String siteId = preferences.getString(getString(R.string.pref_site_id), getString(R.string.site_id));
        return new GliaWidgetsConfig.Builder()
                .setAppToken(appToken)
                .setSiteId(siteId)
                .setRegion("beta")
                .setContext(getApplicationContext())
                .build();
    }

    private GliaWidgetsConfig configWithSiteApiKeyAuth(SharedPreferences preferences) {
        String apiKeyId = preferences.getString(getString(R.string.pref_api_key_id), getString(R.string.glia_api_key_id));
        String apiKeySecret = preferences.getString(getString(R.string.pref_api_key_secret), getString(R.string.glia_api_key_secret));
        String siteId = preferences.getString(getString(R.string.pref_site_id), getString(R.string.site_id));
        return new GliaWidgetsConfig.Builder()
                .setSiteApiKey(new SiteApiKey(apiKeyId, apiKeySecret))
                .setSiteId(siteId)
                .setRegion("beta")
                .setContext(getApplicationContext())
                .build();
    }
}
