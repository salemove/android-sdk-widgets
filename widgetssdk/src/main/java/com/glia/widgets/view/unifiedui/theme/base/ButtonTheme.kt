package com.glia.widgets.view.unifiedui.theme.base

import androidx.annotation.ColorInt

internal data class ButtonTheme(
    val text: TextTheme?,
    val background: LayerTheme?,
    val iconColor: ColorTheme?,
    val elevation: Float?,
    @ColorInt
    val shadowColor: Int?
)
