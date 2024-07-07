package com.glia.widgets.call

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.glia.androidsdk.Engagement
import com.glia.widgets.GliaWidgets
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import java.util.ArrayList

internal object CallActivityIntentHelper {

    @JvmStatic
    fun createIntent(context: Context, configuration: Configuration): Intent {
        val sdkConfiguration = configuration.sdkConfiguration ?: throw NullPointerException("WidgetsSdk Configuration can't be null")

        return Intent(context, CallActivity::class.java)
            .putExtra(GliaWidgets.QUEUE_IDS, sdkConfiguration.queueIds?.let { ArrayList(it) })
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, sdkConfiguration.contextAssetId)
            .putExtra(GliaWidgets.UI_THEME, sdkConfiguration.runTimeTheme)
            .putExtra(GliaWidgets.USE_OVERLAY, sdkConfiguration.useOverlay)
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, sdkConfiguration.screenSharingMode)
            .putExtra(GliaWidgets.MEDIA_TYPE, configuration.mediaType)
            .putExtra(GliaWidgets.IS_UPGRADE_TO_CALL, configuration.isUpgradeToCall)
    }

    @JvmStatic
    fun readConfiguration(activity: AppCompatActivity): Configuration {
        val intent = activity.intent
        val sdkConfiguration = GliaSdkConfiguration.Builder().intent(intent).build()
        val mediaType = intent.getSerializableExtra(GliaWidgets.MEDIA_TYPE) as Engagement.MediaType?
        val isUpgradeToCall = intent.getBooleanExtra(GliaWidgets.IS_UPGRADE_TO_CALL, false)
        return Configuration.Builder()
            .setWidgetsConfiguration(sdkConfiguration)
            .setMediaType(mediaType)
            .setIsUpgradeToCall(isUpgradeToCall)
            .build()
    }
}
