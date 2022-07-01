package com.glia.exampleapp;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.glia.androidsdk.Glia;
import com.glia.widgets.GliaWidgets;

public class Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        initGliaWidgetsWithDeepLink();
    }

    private void initGliaWidgetsWithDeepLink() {
        Uri uri = getIntent().getData();
        if (!Glia.isInitialized() && uri != null) {
            GliaWidgets.init(GliaWidgetsConfigManager.obtainConfigFromDeepLink(uri, getApplicationContext()));
        }
    }
}
