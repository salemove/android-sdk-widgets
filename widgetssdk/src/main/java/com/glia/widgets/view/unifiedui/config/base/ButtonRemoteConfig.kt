package com.glia.widgets.view.unifiedui.config.base

import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.google.gson.annotations.SerializedName

internal data class ButtonRemoteConfig(
    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("tintColor")
    val tintColor: ColorLayerRemoteConfig?,

    @SerializedName("shadow")
    val shadowRemoteConfig: ShadowRemoteConfig?
) {

    fun toButtonTheme(): ButtonTheme = ButtonTheme(
        text = textRemoteConfig?.toTextTheme(),
        background = background?.toLayerTheme(),
        iconColor = tintColor?.toColorTheme(),
        elevation = shadowRemoteConfig?.elevationPx,
        shadowColor = shadowRemoteConfig?.color
    )

}