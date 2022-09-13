package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.FontRemoteConfig
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.google.gson.annotations.SerializedName

internal data class BadgeRemoteConfig(
    @SerializedName("font")
    val fontRemoteConfig: FontRemoteConfig?,

    @SerializedName("fontColor")
    val fontColor: ColorLayerRemoteConfig?,

    @SerializedName("backgroundColor")
    val backgroundColor: ColorLayerRemoteConfig?,
) {

    fun toTextTheme() = TextTheme(
        textColor = fontColor?.toColorTheme(),
        backgroundColor = backgroundColor?.toColorTheme(),
        textSize = fontRemoteConfig?.size?.value,
        textStyle = fontRemoteConfig?.style?.style,
        textAlignment = null
    )
}
