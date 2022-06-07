package com.glia.exampleapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.glia.androidsdk.Glia;
import com.glia.widgets.GliaWidgets;

public class Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        initGliaWidgets();
    }

    private void initGliaWidgets() {
        if (Glia.isInitialized()) return;

        GliaWidgets.init(GliaWidgetsConfigManager.obtainConfig(getIntent(), getApplicationContext()));
    }
}
