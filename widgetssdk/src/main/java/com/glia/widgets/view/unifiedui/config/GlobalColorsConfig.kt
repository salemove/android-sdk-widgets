package com.glia.widgets.view.unifiedui.config

import com.glia.widgets.view.unifiedui.config.base.ColorRemoteConfig
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.google.gson.annotations.SerializedName

internal data class GlobalColorsConfig(
    @SerializedName("baseDark")
    val darkColorConfig: ColorRemoteConfig?,
    @SerializedName("baseLight")
    val lightColorConfig: ColorRemoteConfig?,
    @SerializedName("baseNeutral")
    val neutralColorConfig: ColorRemoteConfig?,
    @SerializedName("baseNormal")
    val normalColorConfig: ColorRemoteConfig?,
    @SerializedName("baseShade")
    val shadeColorConfig: ColorRemoteConfig?,
    @SerializedName("primary")
    val primaryColorConfig: ColorRemoteConfig?,
    @SerializedName("secondary")
    val secondaryColorConfig: ColorRemoteConfig?,
    @SerializedName("systemNegative")
    val negativeColorConfig: ColorRemoteConfig?
) {
    fun toColorPallet(): ColorPallet = ColorPallet(
        darkColorTheme = darkColorConfig?.toColorTheme(),
        lightColorTheme = lightColorConfig?.toColorTheme(),
        neutralColorTheme = neutralColorConfig?.toColorTheme(),
        normalColorTheme = normalColorConfig?.toColorTheme(),
        shadeColorTheme = shadeColorConfig?.toColorTheme(),
        primaryColorTheme = primaryColorConfig?.toColorTheme(),
        secondaryColorTheme = secondaryColorConfig?.toColorTheme(),
        negativeColorTheme = negativeColorConfig?.toColorTheme()
    )
}
