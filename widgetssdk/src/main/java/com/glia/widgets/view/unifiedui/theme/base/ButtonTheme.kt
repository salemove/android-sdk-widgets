package com.glia.widgets.view.unifiedui.theme.base

import androidx.annotation.ColorInt

internal data class ButtonTheme(
    val text: TextTheme? = null,
    val background: LayerTheme? = null,
    val iconColor: ColorTheme? = null,
    val elevation: Float? = null,
    @ColorInt
    val shadowColor: Int? = null
)
