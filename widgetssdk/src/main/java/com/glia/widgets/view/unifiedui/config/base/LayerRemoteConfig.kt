package com.glia.widgets.view.unifiedui.config.base

import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.google.gson.annotations.SerializedName

internal data class LayerRemoteConfig(
    @SerializedName("color")
    val color: ColorLayerRemoteConfig?,

    @SerializedName("border")
    val borderColor: ColorLayerRemoteConfig?,

    @SerializedName("borderWidth")
    val borderWidth: SizeDpRemoteConfig?,

    @SerializedName("cornerRadius")
    val cornerRadius: SizeDpRemoteConfig?
) {
    fun toLayerTheme() = LayerTheme(
        fill = color?.toColorTheme(),
        stroke = borderColor?.toColorTheme()?.primaryColor,
        borderWidth = borderWidth?.valuePx,
        cornerRadius = cornerRadius?.valuePx
    )
}
