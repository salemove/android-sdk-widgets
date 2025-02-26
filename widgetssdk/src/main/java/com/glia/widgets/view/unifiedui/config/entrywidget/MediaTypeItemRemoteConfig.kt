package com.glia.widgets.view.unifiedui.config.entrywidget

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.BadgeRemoteConfig
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemTheme
import com.google.gson.annotations.SerializedName

internal data class MediaTypeItemRemoteConfig(
    @SerializedName("background")
    val background: LayerRemoteConfig?,
    @SerializedName("iconColor")
    val iconColor: ColorLayerRemoteConfig?,
    @SerializedName("title")
    val title: TextRemoteConfig?,
    @SerializedName("message")
    val message: TextRemoteConfig?,
    @SerializedName("loadingTintColor")
    val loadingTintColor: ColorLayerRemoteConfig?,
    @SerializedName("badge")
    val badge: BadgeRemoteConfig?
) {
    fun toMediaTypeItemTheme(): MediaTypeItemTheme = MediaTypeItemTheme(
        background = background?.toLayerTheme(),
        iconColor = iconColor?.toColorTheme(),
        title = title?.toTextTheme(),
        message = message?.toTextTheme(),
        loadingTintColor = loadingTintColor?.toColorTheme(),
        badge = badge?.toBadgeTheme()
    )
}
