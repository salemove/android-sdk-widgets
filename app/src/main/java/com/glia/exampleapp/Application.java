package com.glia.exampleapp;

import android.text.TextUtils;
import android.util.Log;

import com.glia.widgets.GliaWidgets;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initFirebase();

        GliaWidgets.setCustomCardAdapter(new ExampleCustomCardAdapter());
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
}
