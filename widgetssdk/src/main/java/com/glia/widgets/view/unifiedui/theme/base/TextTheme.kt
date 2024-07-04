package com.glia.widgets.view.unifiedui.theme.base

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge

internal data class TextTheme(
    val textColor: ColorTheme? = null,
    val backgroundColor: ColorTheme? = null,
    val textSize: Float? = null, // Size in SP
    val textStyle: Int? = null, // Typeface.NORMAL
    val textAlignment: Int? = null // TextView.TEXT_ALIGNMENT_TEXT_START
) : Mergeable<TextTheme> {
    override fun merge(other: TextTheme): TextTheme = TextTheme(
        textColor = textColor merge other.textColor,
        backgroundColor = backgroundColor merge other.backgroundColor,
        textSize = textSize merge other.textSize,
        textStyle = textStyle merge other.textStyle,
        textAlignment = textAlignment merge other.textAlignment
    )
}
