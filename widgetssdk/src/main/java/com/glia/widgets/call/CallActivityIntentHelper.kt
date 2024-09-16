package com.glia.widgets.call

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.glia.androidsdk.Engagement
import com.glia.widgets.GliaWidgets
import com.glia.widgets.core.configuration.EngagementConfiguration
import com.glia.widgets.helper.getSerializableExtraCompat

internal object CallActivityIntentHelper {

    fun createIntent(context: Context, callConfiguration: CallConfiguration): Intent {
        val engagementConfiguration =
            callConfiguration.engagementConfiguration ?: throw NullPointerException("WidgetsSdk Configuration can't be null")

        return Intent(context, CallActivity::class.java)
            .putExtra(GliaWidgets.QUEUE_IDS, engagementConfiguration.queueIds?.let(::ArrayList))
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, engagementConfiguration.contextAssetId)
            .putExtra(GliaWidgets.UI_THEME, engagementConfiguration.runTimeTheme)
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, engagementConfiguration.screenSharingMode)
            .putExtra(GliaWidgets.MEDIA_TYPE, callConfiguration.mediaType)
            .putExtra(GliaWidgets.IS_UPGRADE_TO_CALL, callConfiguration.isUpgradeToCall)
    }

    fun readConfiguration(activity: AppCompatActivity): CallConfiguration {
        val intent = activity.intent
        val engagementConfiguration = EngagementConfiguration(intent)
        val mediaType = intent.getSerializableExtraCompat<Engagement.MediaType>(GliaWidgets.MEDIA_TYPE)
        val isUpgradeToCall = intent.getBooleanExtra(GliaWidgets.IS_UPGRADE_TO_CALL, false)
        return CallConfiguration(engagementConfiguration, mediaType, isUpgradeToCall)
    }
}
