package com.glia.exampleapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import com.glia.androidsdk.Glia;
import com.glia.widgets.GliaWidgets;

public class TestingAppLauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        initGliaWidgetsWithDeepLink();
    }

    private void initGliaWidgetsWithDeepLink() {
        Uri uri = getIntent().getData();
        if (!Glia.isInitialized() && uri != null) {
            GliaWidgets.init(ExampleAppConfigManager.obtainConfigFromDeepLink(uri, getApplicationContext()));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Navigation.findNavController(this, R.id.nav_host_fragment).handleDeepLink(intent);
    }
}
