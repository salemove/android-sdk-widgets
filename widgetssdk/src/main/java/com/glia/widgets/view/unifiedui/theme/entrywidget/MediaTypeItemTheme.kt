package com.glia.widgets.view.unifiedui.theme.entrywidget

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.BadgeTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class MediaTypeItemTheme(
    val background: LayerTheme? = null,
    val iconColor: ColorTheme? = null,
    val title: TextTheme? = null,
    val message: TextTheme? = null,
    val loadingTintColor: ColorTheme? = null,
    val badge: BadgeTheme? = null
) : Mergeable<MediaTypeItemTheme> {
    override fun merge(other: MediaTypeItemTheme): MediaTypeItemTheme = MediaTypeItemTheme(
        background = background merge other.background,
        iconColor = iconColor merge other.iconColor,
        title = title merge other.title,
        message = message merge other.message,
        loadingTintColor = loadingTintColor merge other.loadingTintColor,
        badge = badge merge other.badge
    )
}
