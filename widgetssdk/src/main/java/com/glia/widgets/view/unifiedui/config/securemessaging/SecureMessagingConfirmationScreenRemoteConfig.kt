package com.glia.widgets.view.unifiedui.config.securemessaging

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.HeaderRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingConfirmationScreenTheme
import com.google.gson.annotations.SerializedName

internal data class SecureMessagingConfirmationScreenRemoteConfig(
    @SerializedName("header")
    val header: HeaderRemoteConfig?,
    @SerializedName("background")
    val background: ColorLayerRemoteConfig?,
    @SerializedName("iconColor")
    val iconColor: ColorLayerRemoteConfig?,
    @SerializedName("title")
    val title: TextRemoteConfig?,
    @SerializedName("subtitle")
    val subtitle: TextRemoteConfig?,
    @SerializedName("checkMessagesButton")
    val checkMessagesButton: ButtonRemoteConfig?
) {
    fun toSecureMessagingConfirmationScreenTheme(): SecureMessagingConfirmationScreenTheme =
        SecureMessagingConfirmationScreenTheme(
            headerTheme = header?.toHeaderTheme(),
            backgroundTheme = background?.toColorTheme(),
            iconColorTheme = iconColor?.toColorTheme(),
            titleTheme = title?.toTextTheme(),
            subtitleTheme = subtitle?.toTextTheme(),
            checkMessagesButtonTheme = checkMessagesButton?.toButtonTheme()
        )
}
