package com.glia.exampleapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.SiteApiKey;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.GliaWidgetsConfig;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GliaWidgets.onAppCreate(this);

        initGliaWidgets();
    }

    private void initGliaWidgets() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (activity.getClass() != com.glia.exampleapp.Activity.class) {
                    if (Glia.isInitialized()) return;

                    GliaWidgets.init(GliaWidgetsConfigManager.createDefaultConfig(getApplicationContext()));
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }

    private GliaWidgetsConfig configWithSiteApiKeyAuth(SharedPreferences preferences) {
        String apiKeyId = preferences.getString(getString(R.string.pref_api_key_id), getString(R.string.glia_api_key_id));
        String apiKeySecret = preferences.getString(getString(R.string.pref_api_key_secret), getString(R.string.glia_api_key_secret));
        String siteId = preferences.getString(getString(R.string.pref_site_id), getString(R.string.site_id));
        return new GliaWidgetsConfig.Builder()
                .setSiteApiKey(new SiteApiKey(apiKeyId, apiKeySecret))
                .setSiteId(siteId)
                .setRegion("beta")
//                .setStyle() // (path to local file or url)
                .setContext(getApplicationContext())
                .build();
    }
}
