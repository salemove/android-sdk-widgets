package com.glia.exampleapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.glia.widgets.GliaWidgets;

public class TestingAppLauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        initGliaWidgetsWithDeepLink();
        GliaWidgets.getPushNotifications().handlePushNotificationClick(getIntent());
    }

    private void initGliaWidgetsWithDeepLink() {
        Uri uri = getIntent().getData();
        if (!GliaWidgets.isInitialized() && uri != null) {
            GliaWidgets.init(
                ExampleAppConfigManager.obtainConfigFromDeepLink(uri, getApplicationContext()),
                () -> runOnUiThread(this::refreshMainScreen),
                error -> runOnUiThread(() -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show())
            );
        }
    }

    // A main screen that is not resumed yet refreshes itself in onResume.
    private void refreshMainScreen() {
        if (isDestroyed()) return;
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment current = navHost.getChildFragmentManager().getPrimaryNavigationFragment();
        if (current instanceof MainFragment && current.isResumed()) {
            ((MainFragment) current).onGliaWidgetsInitialized();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Without this getIntent() keeps returning the intent the Activity was first started with.
        setIntent(intent);

        GliaWidgets.getPushNotifications().handlePushNotificationClick(intent);
        Navigation.findNavController(this, R.id.nav_host_fragment).handleDeepLink(intent);
    }
}
