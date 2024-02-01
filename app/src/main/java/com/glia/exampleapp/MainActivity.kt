package com.glia.exampleapp

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.preference.PreferenceManager
import com.glia.androidsdk.Glia
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.exampleapp.ExampleAppConfigManager.obtainConfigFromDeepLink
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.ChatActivity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val uri = intent.data
        if (!Glia.isInitialized() && uri != null) {
            GliaWidgets.init(obtainConfigFromDeepLink(uri, applicationContext))
        }

        findViewById<Button>(R.id.visitor_code).setOnClickListener {
            GliaWidgets.getCallVisualizer().showVisitorCodeDialog(this)
        }

        findViewById<Button>(R.id.start_chat).setOnClickListener {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val intent = ChatActivity.getIntent(
                this,
                getContextAssetIdFromPrefs(sharedPreferences),
                getQueueIdFromPrefs(sharedPreferences)
            )
            startActivity(intent)
        }
    }

    private fun getUseOverlay(sharedPreferences: SharedPreferences): Boolean {
        return Utils.getUseOverlay(sharedPreferences, resources)
    }

    private fun getScreenSharingModeFromPrefs(sharedPreferences: SharedPreferences): ScreenSharing.Mode {
        return Utils.getScreenSharingModeFromPrefs(sharedPreferences, resources)
    }

    private fun getRuntimeThemeFromPrefs(sharedPreferences: SharedPreferences): UiTheme? {
        return Utils.getRunTimeThemeByPrefs(sharedPreferences, resources)
    }

    private fun getQueueIdFromPrefs(sharedPreferences: SharedPreferences): String {
        return Utils.getStringFromPrefs(
            R.string.pref_queue_id,
            getString(R.string.glia_queue_id),
            sharedPreferences,
            resources
        )
    }

    private fun getContextAssetIdFromPrefs(sharedPreferences: SharedPreferences): String? {
        return Utils.getStringFromPrefs(
            R.string.pref_context_asset_id,
            null,
            sharedPreferences,
            resources
        )
    }
}
