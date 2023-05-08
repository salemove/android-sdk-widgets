package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.FontRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.theme.base.BadgeTheme
import com.google.gson.annotations.SerializedName

internal data class BadgeRemoteConfig(
    @SerializedName("font")
    val fontRemoteConfig: FontRemoteConfig?,

    @SerializedName("fontColor")
    val fontColor: ColorLayerRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?
) {

    fun toBadgeTheme() = BadgeTheme(
        textColor = fontColor?.toColorTheme(),
        background = background?.toLayerTheme(),
        textSize = fontRemoteConfig?.size?.value,
        textStyle = fontRemoteConfig?.style?.style
    )
}
