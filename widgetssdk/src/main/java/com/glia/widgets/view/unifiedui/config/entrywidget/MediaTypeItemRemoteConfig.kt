package com.glia.widgets.view.unifiedui.config.entrywidget

import com.glia.widgets.view.unifiedui.config.base.ColorRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemTheme
import com.google.gson.annotations.SerializedName

internal data class MediaTypeItemRemoteConfig(
    @SerializedName("background")
    val background: LayerRemoteConfig?,
    @SerializedName("iconColor")
    val iconColor: ColorRemoteConfig?,
    @SerializedName("title")
    val title: TextRemoteConfig?,
    @SerializedName("message")
    val message: TextRemoteConfig?
) {
    fun toMediaTypeItemTheme(): MediaTypeItemTheme = MediaTypeItemTheme(
        background = background?.toLayerTheme(),
        iconColor = iconColor?.toColorTheme(),
        title = title?.toTextTheme(),
        message = message?.toTextTheme()
    )
}
