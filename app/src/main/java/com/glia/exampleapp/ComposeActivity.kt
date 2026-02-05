package com.glia.exampleapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.glia.androidsdk.Glia
import com.glia.widgets.GliaWidgets
import com.glia.exampleapp.ui.navigation.AppNavigation
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

/**
 * Main Compose-based activity for the testing app.
 * Replaces the Fragment-based TestingAppLauncherActivity.
 */
class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle deep link configuration
        initGliaWidgetsWithDeepLink()

        setContent {
            GliaExampleAppTheme {
                AppNavigation()
            }
        }
    }

    private fun initGliaWidgetsWithDeepLink() {
        val uri = intent.data
        if (!Glia.isInitialized() && uri != null) {
            GliaWidgets.init(ExampleAppConfigManager.obtainConfigFromDeepLink(uri, applicationContext))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new deep link intents
        setIntent(intent)
        initGliaWidgetsWithDeepLink()
    }
}
