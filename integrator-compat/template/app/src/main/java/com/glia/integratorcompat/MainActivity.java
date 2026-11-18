package com.glia.integratorcompat;

import android.app.Activity;
import android.os.Bundle;

import com.glia.widgets.AuthorizationMethod;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.GliaWidgetsConfig;
import com.glia.widgets.Region;

import java.util.Collections;

// The app is only built, never run, so the configuration values are placeholders
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GliaWidgetsConfig config = new GliaWidgetsConfig.Builder()
            .setAuthorizationMethod(new AuthorizationMethod.UserApiKey("api-key-id", "api-key-secret"))
            .setSiteId("site-id")
            .setRegion(Region.US.INSTANCE)
            .setContext(getApplicationContext())
            .build();

        GliaWidgets.init(
            config,
            () -> GliaWidgets.getEngagementLauncher(Collections.singletonList("queue-id")).startChat(this),
            exception -> { }
        );
    }
}
