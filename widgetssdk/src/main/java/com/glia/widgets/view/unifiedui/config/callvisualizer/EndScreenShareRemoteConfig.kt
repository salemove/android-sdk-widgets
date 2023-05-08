package com.glia.widgets.view.unifiedui.config.callvisualizer

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.HeaderRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.EndScreenSharingTheme
import com.google.gson.annotations.SerializedName

internal data class EndScreenShareRemoteConfig(

    @SerializedName("header")
    val headerRemoteConfig: HeaderRemoteConfig?,

    @SerializedName("message")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("endButton")
    val buttonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?

) {
    fun toEndScreenShareTheme(): EndScreenSharingTheme = EndScreenSharingTheme(
        header = headerRemoteConfig?.toHeaderTheme(),
        label = textRemoteConfig?.toTextTheme(),
        endButton = buttonRemoteConfig?.toButtonTheme(),
        background = background?.toLayerTheme()
    )
}
