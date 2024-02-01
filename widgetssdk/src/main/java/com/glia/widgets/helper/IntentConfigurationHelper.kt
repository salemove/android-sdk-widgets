package com.glia.widgets.helper

import android.content.Context
import android.content.Intent
import com.glia.androidsdk.Engagement.MediaType
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.Configuration
import com.glia.widgets.di.Dependencies

internal object IntentConfigurationHelper {
    private val defaultBuilder: Configuration.Builder
        get() = Dependencies.getSdkConfigurationManager()
            .createWidgetsConfiguration()
            .let(Configuration.Builder()::setWidgetsConfiguration)

    fun createForCall(context: Context, mediaType: MediaType, upgradeToCall: Boolean = true): Intent = defaultBuilder
        .setMediaType(mediaType)
        .setIsUpgradeToCall(upgradeToCall)
        .run { CallActivity.getIntent(context, build()) }
}
