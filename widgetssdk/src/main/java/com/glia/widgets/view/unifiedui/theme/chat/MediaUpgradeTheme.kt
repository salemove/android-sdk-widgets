package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class MediaUpgradeTheme(
    val text: TextTheme? = null,
    val description: TextTheme? = null,
    val iconColor: ColorTheme? = null,
    val background: LayerTheme? = null
) : Mergeable<MediaUpgradeTheme> {
    override fun merge(other: MediaUpgradeTheme): MediaUpgradeTheme = MediaUpgradeTheme(
        text = text merge other.text,
        description = description merge other.description,
        iconColor = iconColor merge other.iconColor,
        background = background merge other.background
    )
}
