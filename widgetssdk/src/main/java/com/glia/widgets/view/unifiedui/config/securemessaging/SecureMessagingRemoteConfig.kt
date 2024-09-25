package com.glia.widgets.view.unifiedui.config.securemessaging

import com.glia.widgets.view.unifiedui.config.base.ColorRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.config.entrywidget.MediaTypeItemsRemoteConfig
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingTheme
import com.google.gson.annotations.SerializedName

internal data class SecureMessagingRemoteConfig(
    @SerializedName("unavailableStatusBackground")
    val unavailableStatusBackground: LayerRemoteConfig?,
    @SerializedName("unavailableStatusText")
    val unavailableStatusText: TextRemoteConfig?,
    @SerializedName("bottomBannerBackground")
    val bottomBannerBackground: LayerRemoteConfig?,
    @SerializedName("bottomBannerText")
    val bottomBannerText: TextRemoteConfig?,
    @SerializedName("bottomBannerDividerColor")
    val bottomBannerDividerColor: ColorRemoteConfig?,
    @SerializedName("topBannerBackground")
    val topBannerBackground: LayerRemoteConfig?,
    @SerializedName("topBannerText")
    val topBannerText: TextRemoteConfig?,
    @SerializedName("topBannerDividerColor")
    val topBannerDividerColor: ColorRemoteConfig?,
    @SerializedName("topBannerDropDownIconColor")
    val topBannerDropDownIconColor: ColorRemoteConfig?,
    @SerializedName("mediaTypeItems")
    val mediaTypeItems: MediaTypeItemsRemoteConfig?
) {
    fun toSecureMessagingTheme(): SecureMessagingTheme = SecureMessagingTheme(
        unavailableStatusBackground = unavailableStatusBackground?.toLayerTheme(),
        unavailableStatusText = unavailableStatusText?.toTextTheme(),
        bottomBannerBackground = bottomBannerBackground?.toLayerTheme(),
        bottomBannerText = bottomBannerText?.toTextTheme(),
        bottomBannerDividerColor = bottomBannerDividerColor?.toColorTheme(),
        topBannerBackground = topBannerBackground?.toLayerTheme(),
        topBannerText = topBannerText?.toTextTheme(),
        topBannerDividerColor = topBannerDividerColor?.toColorTheme(),
        topBannerDropDownIconColor = topBannerDropDownIconColor?.toColorTheme(),
        mediaTypeItems = mediaTypeItems?.toMediaTypeItemsTheme()
    )
}
