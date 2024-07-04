package com.glia.widgets.view.unifiedui.theme.base

import androidx.annotation.ColorInt
import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge

internal data class ButtonTheme(
    val text: TextTheme? = null,
    val background: LayerTheme? = null,
    val iconColor: ColorTheme? = null,
    val elevation: Float? = null,
    @ColorInt
    val shadowColor: Int? = null
) : Mergeable<ButtonTheme> {
    override fun merge(other: ButtonTheme): ButtonTheme = ButtonTheme(
        text = text merge other.text,
        background = background merge other.background,
        iconColor = iconColor merge other.iconColor,
        elevation = other.elevation ?: elevation,
        shadowColor = other.shadowColor ?: shadowColor
    )
}
