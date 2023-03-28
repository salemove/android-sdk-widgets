package com.glia.widgets.view.unifiedui.config.base

import com.glia.widgets.view.unifiedui.theme.base.TextInputTheme
import com.google.gson.annotations.SerializedName

/**
 * Represents text input style from remote config
 */
internal data class TextInputRemoteConfig(
    @SerializedName("text")
    val text: TextRemoteConfig?,
    @SerializedName("background")
    val background: LayerRemoteConfig?
) {
    fun toTextInputTheme(): TextInputTheme = TextInputTheme(
        textTheme = text?.toTextTheme(),
        backgroundTheme = background?.toLayerTheme()
    )
}
