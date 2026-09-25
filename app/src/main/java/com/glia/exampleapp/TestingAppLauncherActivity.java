package com.glia.exampleapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

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
                () -> { },
                error -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show()
            );
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
