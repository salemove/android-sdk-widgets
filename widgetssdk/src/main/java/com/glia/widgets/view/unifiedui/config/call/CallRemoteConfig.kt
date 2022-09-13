package com.glia.widgets.view.unifiedui.config.call

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.HeaderRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.call.CallTheme
import com.google.gson.annotations.SerializedName

internal data class CallRemoteConfig(
    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("bottomText")
    val bottomTextRemoteConfig: TextRemoteConfig?,

    @SerializedName("buttonBar")
    val buttonBarRemoteConfig: ButtonBarRemoteConfig?,

    @SerializedName("duration")
    val duration: TextRemoteConfig?,

    @SerializedName("endButton")
    val endButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("header")
    val headerRemoteConfig: HeaderRemoteConfig?,

    @SerializedName("operator")
    val operator: TextRemoteConfig?,

    @SerializedName("topText")
    val topTextRemoteConfig: TextRemoteConfig?
) {
    fun toCallTheme(): CallTheme = CallTheme(
        background = background?.toLayerTheme(),
        bottomText = bottomTextRemoteConfig?.toTextTheme(),
        buttonBar = buttonBarRemoteConfig?.toButtonBarTheme(),
        duration = duration?.toTextTheme(),
        endButton = endButtonRemoteConfig?.toButtonTheme(),
        header = headerRemoteConfig?.toHeaderTheme(),
        operator = operator?.toTextTheme(),
        topText = topTextRemoteConfig?.toTextTheme()
    )
}