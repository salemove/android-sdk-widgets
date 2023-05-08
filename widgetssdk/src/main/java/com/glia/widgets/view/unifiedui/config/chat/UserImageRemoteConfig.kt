package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.UserImageTheme
import com.google.gson.annotations.SerializedName

internal data class UserImageRemoteConfig(
    @SerializedName("placeholderColor")
    val placeholderColor: ColorLayerRemoteConfig?,

    @SerializedName("placeholderBackgroundColor")
    val placeholderBackgroundColor: ColorLayerRemoteConfig?,

    @SerializedName("imageBackgroundColor")
    val imageBackgroundColor: ColorLayerRemoteConfig?
) {
    fun toUserImageTheme(): UserImageTheme = UserImageTheme(
        placeholderColor = placeholderColor?.toColorTheme(),
        placeholderBackgroundColor = placeholderBackgroundColor?.toColorTheme(),
        imageBackgroundColor = imageBackgroundColor?.toColorTheme()
    )
}
