package com.glia.widgets.view.unifiedui.theme.base

internal data class TextTheme(
    val textColor: ColorTheme? = null,
    val backgroundColor: ColorTheme? = null,
    val textSize: Float? = null, // Size in SP
    val textStyle: Int? = null, // Typeface.NORMAL
    val textAlignment: Int? = null // TextView.TEXT_ALIGNMENT_TEXT_START
)
