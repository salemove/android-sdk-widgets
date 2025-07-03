package com.glia.widgets.view.unifiedui.config.base

import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.google.gson.annotations.SerializedName

internal data class HeaderRemoteConfig(
    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("backButton")
    val backButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("closeButton")
    val closeButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("endButton")
    val endButtonRemoteConfig: ButtonRemoteConfig?
) {
    fun toHeaderTheme(): HeaderTheme = HeaderTheme(
        text = textRemoteConfig?.toTextTheme(),
        background = background?.toLayerTheme(),
        backButton = backButtonRemoteConfig?.toButtonTheme(),
        closeButton = closeButtonRemoteConfig?.toButtonTheme(),
        endButton = endButtonRemoteConfig?.toButtonTheme()
    )
}
