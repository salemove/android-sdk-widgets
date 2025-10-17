package com.glia.exampleapp;

import android.content.Context;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.glia.widgets.BuildConfig;
import com.glia.widgets.GliaWidgets;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class Application extends android.app.Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        if (BuildConfig.DEBUG) {
            setupStrictMode();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initFirebase();

//        GliaWidgets.setCustomCardAdapter(new DeprecatedExampleCustomCardAdapter()); // For testing purposes
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

    /**
     * Configures Android Strict Mode for catching common performance issues.
     */
    private void setupStrictMode() {

        // --- 1. ThreadPolicy Configuration ---
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()          // Detects disk read operations on the main thread.
            .detectDiskWrites()         // Detects disk write operations on the main thread.
            .detectNetwork()            // Detects network operations on the main thread.
            .detectCustomSlowCalls()
            .detectResourceMismatches()
            // Recommended: Log the violation and then flash a visual warning.
            .penaltyLog()               // Prints a stack trace to Logcat. Essential for debugging.
            .penaltyFlashScreen()       // Flashes the screen red/yellow to visually alert the developer.

            // Edge Case: If you have legitimate (though rare) main-thread I/O that
            // you've vetted and want to ignore, you can use .permitDiskReads() etc.
            // However, this is strongly discouraged!

            .build();

        // Note: This is senseless at the moment because all the libraries we use violate thread policy.
//        StrictMode.setThreadPolicy(threadPolicy);

        // --- 2. VmPolicy Configuration ---
        StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectLeakedRegistrationObjects()
            .penaltyLog()
            .penaltyDeath();

        StrictMode.setVmPolicy(vmPolicyBuilder.build());

        Log.i("StrictMode", "Strict Mode enabled for debug build.");
    }
}
