package com.glia.exampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.Glia;
import com.glia.widgets.GliaWidgets;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initFirebase();

        GliaWidgets.onAppCreate(this);
        GliaWidgets.setCustomCardAdapter(new ExampleCustomCardAdapter());

        initGliaWidgets();
    }

    private void initFirebase() {
        String projectId = getString(R.string.firebase_proj_id);
        String appId = getString(R.string.firebase_app_id);
        String apiKey = getString(R.string.firebase_api_key);
        if (TextUtils.isEmpty(projectId) || TextUtils.isEmpty(appId) || TextUtils.isEmpty(apiKey)) {
            Log.e("PushNotification", "Failed to find Firebase credentials. Aborting Firebase setup");
            Log.e("PushNotification", "Push notifications will not be available");
            return;
        }

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId(projectId)
                .setApplicationId(appId)
                .setApiKey(apiKey)
                .build();
        FirebaseApp.initializeApp(this, options);
    }

    private void initGliaWidgets() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (activity.getClass() != com.glia.exampleapp.Activity.class) {
                    if (Glia.isInitialized()) return;

                    GliaWidgets.init(ExampleAppConfigManager.createDefaultConfig(getApplicationContext()));
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {}

            @Override
            public void onActivityResumed(@NonNull Activity activity) {}

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }
}
