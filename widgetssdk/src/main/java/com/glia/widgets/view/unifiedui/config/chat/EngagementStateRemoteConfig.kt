package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.EngagementStateTheme
import com.google.gson.annotations.SerializedName

internal data class EngagementStateRemoteConfig(
    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("description")
    val description: TextRemoteConfig?,

    @SerializedName("tintColor")
    val tintColor: ColorLayerRemoteConfig?,
) {
    fun toEngagementStateTheme(): EngagementStateTheme = EngagementStateTheme(
        title = title?.toTextTheme(),
        description = description?.toTextTheme(),
        tintColor = tintColor?.toColorTheme()
    )
}
