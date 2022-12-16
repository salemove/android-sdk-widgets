package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.bubble.BubbleRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.UnreadIndicatorTheme
import com.google.gson.annotations.SerializedName

internal data class UnreadIndicatorRemoteConfig(
    @SerializedName("backgroundColor")
    val background: ColorLayerRemoteConfig?,

    @SerializedName("bubble")
    val bubble: BubbleRemoteConfig?
) {
    fun toUnreadIndicatorTheme(): UnreadIndicatorTheme = UnreadIndicatorTheme(
        background = background?.toColorTheme(),
        bubble = bubble?.toBubbleTheme()
    )
}
