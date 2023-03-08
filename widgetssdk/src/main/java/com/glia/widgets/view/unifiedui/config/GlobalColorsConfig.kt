package com.glia.widgets.view.unifiedui.config

import com.glia.widgets.view.unifiedui.config.base.ColorRemoteConfig
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.google.gson.annotations.SerializedName

internal data class GlobalColorsConfig(
    @SerializedName("background")
    val backgroundColorConfig: ColorRemoteConfig?,
    @SerializedName("baseDark")
    val baseDarkColorConfig: ColorRemoteConfig?,
    @SerializedName("baseLight")
    val baseLightColorConfig: ColorRemoteConfig?,
    @SerializedName("baseNormal")
    val baseNormalColorConfig: ColorRemoteConfig?,
    @SerializedName("baseShade")
    val baseShadeColorConfig: ColorRemoteConfig?,
    @SerializedName("primary")
    val primaryColorConfig: ColorRemoteConfig?,
    @SerializedName("secondary")
    val secondaryColorConfig: ColorRemoteConfig?,
    @SerializedName("systemNegative")
    val systemNegativeColorConfig: ColorRemoteConfig?
) {
    fun toColorPallet(): ColorPallet = ColorPallet(
        backgroundColorConfig?.toColorTheme(),
        baseDarkColorConfig?.toColorTheme(),
        baseLightColorConfig?.toColorTheme(),
        baseNormalColorConfig?.toColorTheme(),
        baseShadeColorConfig?.toColorTheme(),
        primaryColorConfig?.toColorTheme(),
        secondaryColorConfig?.toColorTheme(),
        systemNegativeColorConfig?.toColorTheme()
    )
}
