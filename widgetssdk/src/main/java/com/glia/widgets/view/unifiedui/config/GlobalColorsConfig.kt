package com.glia.widgets.view.unifiedui.config

import com.glia.widgets.view.unifiedui.config.base.ColorRemoteConfig
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.google.gson.annotations.SerializedName

internal data class GlobalColorsConfig(
    @SerializedName("baseDark")
    val baseDarkColorConfig: ColorRemoteConfig?,
    @SerializedName("baseLight")
    val baseLightColorConfig: ColorRemoteConfig?,
    @SerializedName("baseNeutral")
    val baseNeutralColorConfig: ColorRemoteConfig?,
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
        baseDarkColorTheme = baseDarkColorConfig?.toColorTheme(),
        baseLightColorTheme = baseLightColorConfig?.toColorTheme(),
        baseNeutralColorTheme = baseNeutralColorConfig?.toColorTheme(),
        baseNormalColorTheme = baseNormalColorConfig?.toColorTheme(),
        baseShadeColorTheme = baseShadeColorConfig?.toColorTheme(),
        primaryColorTheme = primaryColorConfig?.toColorTheme(),
        secondaryColorTheme = secondaryColorConfig?.toColorTheme(),
        systemNegativeColorTheme = systemNegativeColorConfig?.toColorTheme()
    )
}
