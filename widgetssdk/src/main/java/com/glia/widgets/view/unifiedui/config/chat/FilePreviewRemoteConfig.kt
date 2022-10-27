package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.FilePreviewTheme
import com.google.gson.annotations.SerializedName

internal data class FilePreviewRemoteConfig(
    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("errorIcon")
    val errorIcon: ColorLayerRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("errorBackground")
    val errorBackground: LayerRemoteConfig?
) {
    fun toFilePreviewTheme(): FilePreviewTheme = FilePreviewTheme(
        text = textRemoteConfig?.toTextTheme(),
        errorIcon = errorIcon?.toColorTheme(),
        background = background?.toLayerTheme(),
        errorBackground = errorBackground?.toLayerTheme()
    )
}
