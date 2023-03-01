package com.glia.widgets.view.unifiedui.config.callvisualizer

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.FontRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.NumberSlotTheme
import com.google.gson.annotations.SerializedName

internal data class NumberSlotRemoteConfig(
    @SerializedName("foreground")
    val textColor: ColorLayerRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("font")
    val fontRemoteConfig: FontRemoteConfig?,
) {
    fun toNumberSlotTheme(): NumberSlotTheme = NumberSlotTheme(
        textColor = textColor?.toColorTheme(),
        background = background?.toLayerTheme(),
        textSize = fontRemoteConfig?.size?.value,
        textStyle = fontRemoteConfig?.style?.style
    )

}
