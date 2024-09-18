package com.glia.widgets.call

import androidx.appcompat.app.AppCompatActivity
import com.glia.androidsdk.Engagement
import com.glia.widgets.GliaWidgets
import com.glia.widgets.core.configuration.EngagementConfiguration
import com.glia.widgets.helper.getSerializableExtraCompat

internal object CallActivityConfigurationHelper {

    fun readConfiguration(activity: AppCompatActivity): CallConfiguration {
        val intent = activity.intent
        val engagementConfiguration = EngagementConfiguration(intent)
        val mediaType = intent.getSerializableExtraCompat<Engagement.MediaType>(GliaWidgets.MEDIA_TYPE)
        val isUpgradeToCall = intent.getBooleanExtra(GliaWidgets.IS_UPGRADE_TO_CALL, false)
        return CallConfiguration(engagementConfiguration = engagementConfiguration, mediaType = mediaType, isUpgradeToCall = isUpgradeToCall)
    }
}
