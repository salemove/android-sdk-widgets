package com.glia.widgets.call

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.glia.androidsdk.Engagement
import com.glia.widgets.GliaWidgets
import com.glia.widgets.core.configuration.EngagementConfiguration
import java.util.ArrayList

internal object CallActivityIntentHelper { // Is needed internally only

    @JvmStatic
    fun createIntent(context: Context, callConfiguration: CallConfiguration): Intent {
        val sdkConfiguration = callConfiguration.engagementConfiguration ?: throw NullPointerException("WidgetsSdk Configuration can't be null")

        return Intent(context, CallActivity::class.java)
            .putExtra(GliaWidgets.QUEUE_IDS, sdkConfiguration.queueIds?.let { ArrayList(it) })
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, sdkConfiguration.contextAssetId)
            .putExtra(GliaWidgets.UI_THEME, sdkConfiguration.runTimeTheme)
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, sdkConfiguration.screenSharingMode)
            .putExtra(GliaWidgets.MEDIA_TYPE, callConfiguration.mediaType)
            .putExtra(GliaWidgets.IS_UPGRADE_TO_CALL, callConfiguration.isUpgradeToCall)
    }

    @JvmStatic
    fun readConfiguration(activity: AppCompatActivity): CallConfiguration {
        val intent = activity.intent
        val engagementConfiguration = EngagementConfiguration.Builder().intent(intent).build()
        val mediaType = intent.getSerializableExtra(GliaWidgets.MEDIA_TYPE) as Engagement.MediaType?
        val isUpgradeToCall = intent.getBooleanExtra(GliaWidgets.IS_UPGRADE_TO_CALL, false)
        return CallConfiguration.Builder()
            .setEngagementConfiguration(engagementConfiguration)
            .setMediaType(mediaType)
            .setIsUpgradeToCall(isUpgradeToCall)
            .build()
    }
}
