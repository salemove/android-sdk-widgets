package com.glia.exampleapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.glia.androidsdk.Glia
import com.glia.widgets.GliaWidgets
import com.glia.exampleapp.data.AppState
import com.glia.exampleapp.ui.navigation.AppNavigation
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

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
            val appState = AppState.getInstance(applicationContext)

            lifecycleScope.launch {
                // Get existing configuration from DataStore
                val existingConfig = appState.configuration.firstOrNull() ?: return@launch

                // Parse and merge deep link with existing configuration
                val config = ExampleAppConfigManager.parseDeepLinkToConfiguration(uri, applicationContext, existingConfig)
                if (config != null) {
                    // Save merged configuration to DataStore
                    appState.configurationRepository.updateConfiguration(config)

                    // Initialize SDK with merged configuration
                    val gliaConfig = ExampleAppConfigManager.createConfigFromDataStore(applicationContext, config)
                    GliaWidgets.init(gliaConfig)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new deep link intents
        setIntent(intent)
        initGliaWidgetsWithDeepLink()
    }
}
