package com.glia.widgets.view.unifiedui.config.call

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.call.BarButtonStyleTheme
import com.google.gson.annotations.SerializedName

internal data class BarButtonStyleRemoteConfig(
    @SerializedName("background")
    val background: ColorLayerRemoteConfig?,

    @SerializedName("imageColor")
    val imageColor: ColorLayerRemoteConfig?,

    @SerializedName("title")
    val title: TextRemoteConfig?
) {
    fun toBarButtonTheme() = BarButtonStyleTheme(
        background = background?.toColorTheme(),
        imageColor = imageColor?.toColorTheme(),
        title = title?.toTextTheme()
    )
}