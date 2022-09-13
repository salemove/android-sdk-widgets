package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.OperatorTheme
import com.google.gson.annotations.SerializedName

internal data class OperatorRemoteConfig(
    @SerializedName("image")
    val image: UserImageRemoteConfig?,

    @SerializedName("animationColor")
    val animationColor: ColorLayerRemoteConfig?,

    @SerializedName("overlayColor")
    val overlayColor: ColorLayerRemoteConfig?
) {
    fun toOperatorTheme(): OperatorTheme = OperatorTheme(
        image = image?.toUserImageTheme(),
        animationColor = animationColor?.toColorTheme(),
        overlayColor = overlayColor?.toColorTheme()
    )
}
