package com.glia.widgets.view.unifiedui.config.bubble

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.OnHoldOverlayTheme
import com.google.gson.annotations.SerializedName

internal class OnHoldOverlayRemoteConfig(
    @SerializedName("backgroundColor")
    val backgroundColor: ColorLayerRemoteConfig?,

    @SerializedName("tintColor")
    val tintColor: ColorLayerRemoteConfig?
) {
    fun toOnHoldOverlayTheme(): OnHoldOverlayTheme = OnHoldOverlayTheme(
        backgroundColor = backgroundColor?.toColorTheme(),
        tintColor = tintColor?.toColorTheme()
    )
}
