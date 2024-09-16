package com.glia.widgets.helper

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import com.glia.androidsdk.Engagement.MediaType
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.CallConfiguration
import com.glia.widgets.di.Dependencies

internal interface IntentConfigurationHelper {
    fun createForCall(context: Context, mediaType: MediaType, upgradeToCall: Boolean = true): Intent

    fun createForOverlayPermissionScreen(context: Context): Intent
}

internal class IntentConfigurationHelperImpl : IntentConfigurationHelper {
    private val defaultConfiguration: CallConfiguration
        get() = Dependencies.sdkConfigurationManager
            .buildEngagementConfiguration()
            .let(::CallConfiguration)

    override fun createForCall(context: Context, mediaType: MediaType, upgradeToCall: Boolean): Intent = CallActivity.getIntent(
        context,
        defaultConfiguration.copy(mediaType = mediaType, isUpgradeToCall = upgradeToCall)
    )

    override fun createForOverlayPermissionScreen(context: Context): Intent = context.run {
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${packageName}".toUri()).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
