package com.glia.widgets.view.unifiedui.theme.base

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge

internal data class BadgeTheme(
    val textColor: ColorTheme? = null,
    val background: LayerTheme? = null,
    val textSize: Float? = null, // Size in SP
    val textStyle: Int? = null // Typeface.NORMAL
) : Mergeable<BadgeTheme> {
    override fun merge(other: BadgeTheme): BadgeTheme = BadgeTheme(
        textColor = textColor merge other.textColor,
        background = background merge other.background,
        textSize = textSize merge other.textSize,
        textStyle = textStyle merge other.textStyle
    )
}
