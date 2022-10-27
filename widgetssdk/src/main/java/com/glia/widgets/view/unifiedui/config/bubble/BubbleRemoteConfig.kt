package com.glia.widgets.view.unifiedui.config.bubble

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.UserImageRemoteConfig
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme
import com.google.gson.annotations.SerializedName

internal data class BubbleRemoteConfig(

    @SerializedName("userImage")
    val userImage: UserImageRemoteConfig?,

    @SerializedName("badge")
    val badge: ButtonRemoteConfig?,

    @SerializedName("onHoldOverlay")
    val onHoldOverlay: OnHoldOverlayRemoteConfig?
) {
    fun toBubbleTheme(): BubbleTheme = BubbleTheme(
        userImage = userImage?.toUserImageTheme(),
        badge = badge?.toButtonTheme(),
        onHoldOverlay = onHoldOverlay?.color?.toColorTheme()
    )
}
