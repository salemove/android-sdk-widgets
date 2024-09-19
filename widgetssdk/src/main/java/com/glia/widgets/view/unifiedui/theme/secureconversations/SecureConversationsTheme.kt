package com.glia.widgets.view.unifiedui.theme.secureconversations

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemsTheme

internal data class SecureConversationsTheme(
    val unavailableStatusBackground: LayerTheme?,
    val unavailableStatusText: TextTheme?,
    val bottomBannerBackground: LayerTheme?,
    val bottomBannerText: TextTheme?,
    val bottomBannerDividerColor: ColorTheme?,
    val topBannerBackground: LayerTheme?,
    val topBannerText: TextTheme?,
    val topBannerDividerColor: ColorTheme?,
    val topBannerDropDownIconColor: ColorTheme?,
    val mediaTypeItems: MediaTypeItemsTheme?
) : Mergeable<SecureConversationsTheme> {
    override fun merge(other: SecureConversationsTheme): SecureConversationsTheme = SecureConversationsTheme(
        unavailableStatusBackground = unavailableStatusBackground merge other.unavailableStatusBackground,
        unavailableStatusText = unavailableStatusText merge other.unavailableStatusText,
        bottomBannerBackground = bottomBannerBackground merge other.bottomBannerBackground,
        bottomBannerText = bottomBannerText merge other.bottomBannerText,
        bottomBannerDividerColor = bottomBannerDividerColor merge other.bottomBannerDividerColor,
        topBannerBackground = topBannerBackground merge other.topBannerBackground,
        topBannerText = topBannerText merge other.topBannerText,
        topBannerDividerColor = topBannerDividerColor merge other.topBannerDividerColor,
        topBannerDropDownIconColor = topBannerDropDownIconColor merge other.topBannerDropDownIconColor,
        mediaTypeItems = mediaTypeItems merge other.mediaTypeItems
    )
}
