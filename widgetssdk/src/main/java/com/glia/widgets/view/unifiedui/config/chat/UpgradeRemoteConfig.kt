package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.MediaUpgradeTheme
import com.google.gson.annotations.SerializedName

internal data class UpgradeRemoteConfig(

    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("description")
    val description: TextRemoteConfig?,

    @SerializedName("iconColor")
    val iconColor: ColorLayerRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,
) {
    fun toUpgradeTheme(): MediaUpgradeTheme = MediaUpgradeTheme(
        text = textRemoteConfig?.toTextTheme(),
        description = description?.toTextTheme(),
        iconColor = iconColor?.toColorTheme(),
        background = background?.toLayerTheme()
    )
}
