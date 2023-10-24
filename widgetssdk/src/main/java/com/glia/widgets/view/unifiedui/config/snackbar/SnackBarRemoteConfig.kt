package com.glia.widgets.view.unifiedui.config.snackbar

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.theme.SnackBarTheme
import com.google.gson.annotations.SerializedName

internal data class SnackBarRemoteConfig(
    @SerializedName("background")
    val background: ColorLayerRemoteConfig?,
    @SerializedName("text")
    val text: ColorLayerRemoteConfig?
) {
    fun toSnackBarTheme(): SnackBarTheme = SnackBarTheme(
        backgroundColorTheme = background?.toColorTheme(),
        textColorTheme = text?.toColorTheme()
    )
}
