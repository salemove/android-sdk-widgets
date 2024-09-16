package com.glia.widgets.core.configuration

import android.content.Intent
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.ChatType
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getParcelableExtraCompat
import com.glia.widgets.helper.getSerializableExtraCompat

internal data class EngagementConfiguration(
    val companyName: String? = null,
    val queueIds: List<String>? = null,
    val contextAssetId: String? = null,
    val chatType: ChatType? = null,
    private val _runTimeTheme: UiTheme? = null,
    private val _screenSharingMode: ScreenSharing.Mode? = null
) {
    constructor(intent: Intent) : this(
        companyName = Dependencies.sdkConfigurationManager.companyName,
        queueIds = intent.getStringArrayListExtra(GliaWidgets.QUEUE_IDS) ?: intent.getStringExtra(GliaWidgets.QUEUE_ID)?.let(::listOf),
        contextAssetId = intent.getStringExtra(GliaWidgets.CONTEXT_ASSET_ID),
        chatType = intent.getParcelableExtraCompat(GliaWidgets.CHAT_TYPE),
        _runTimeTheme = intent.getParcelableExtraCompat(GliaWidgets.UI_THEME) ?: Dependencies.sdkConfigurationManager.uiTheme,
        _screenSharingMode = intent.getSerializableExtraCompat(GliaWidgets.SCREEN_SHARING_MODE)
            ?: Dependencies.sdkConfigurationManager.screenSharingMode
    )

    val runTimeTheme: UiTheme?
        get() = _runTimeTheme ?: Dependencies.sdkConfigurationManager.uiTheme

    val screenSharingMode: ScreenSharing.Mode?
        get() = _screenSharingMode ?: Dependencies.sdkConfigurationManager.screenSharingMode
}
