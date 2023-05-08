package com.glia.widgets.view.unifiedui.config.base

import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.google.gson.annotations.SerializedName

internal data class TextRemoteConfig(
    @SerializedName("foreground")
    val textColor: ColorLayerRemoteConfig?,

    @SerializedName("background")
    val backgroundColor: ColorLayerRemoteConfig?,

    @SerializedName("font")
    val fontRemoteConfig: FontRemoteConfig?,

    @SerializedName("alignment")
    val alignment: AlignmentTypeRemoteConfig?
) {
    fun toTextTheme(): TextTheme = TextTheme(
        textColor = textColor?.toColorTheme(),
        backgroundColor = backgroundColor?.toColorTheme(),
        textSize = fontRemoteConfig?.size?.value,
        textStyle = fontRemoteConfig?.style?.style,
        textAlignment = alignment?.nativeAlignment
    )
}
