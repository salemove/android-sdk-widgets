package com.glia.exampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.android.domificator.Domificator;
import com.glia.androidsdk.Glia;
import com.glia.widgets.GliaWidgets;

import java.lang.ref.WeakReference;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GliaWidgets.onAppCreate(this);
        GliaWidgets.setCustomCardAdapter(new ExampleCustomCardAdapter());

        initGliaWidgets();
    }

    private static class DomReadingRunnable implements Runnable {
        private WeakReference<Activity> activityRef;
        private final Domificator domificator;
        private final Handler handler;

        @MainThread // Did not create a loop for Handler
        public DomReadingRunnable(Domificator domificator) {
            this.domificator = domificator;
            this.handler = new Handler();
            run();
        }

        @Override
        public void run() {
            handler.postDelayed(this, 200);

            Activity activity = activityRef == null ? null : activityRef.get();
            if (activity == null) return;
            String htmlDom = domificator.generateDomFromActivity(activity);
            Log.e(Domificator.class.getSimpleName(), "Tik-tak");
        }

        public void setCurrentActivity(Activity activity) {
            activityRef = new WeakReference<>(activity);
        }
    }

    private void initGliaWidgets() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            DomReadingRunnable domReader = new DomReadingRunnable(new Domificator());

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (activity.getClass() != com.glia.exampleapp.Activity.class) {
                    if (Glia.isInitialized()) return;

                    GliaWidgets.init(GliaWidgetsConfigManager.createDefaultConfig(getApplicationContext()));
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {}

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                domReader.setCurrentActivity(activity);
            }

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
