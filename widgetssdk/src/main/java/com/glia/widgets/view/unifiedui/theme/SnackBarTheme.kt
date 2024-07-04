package com.glia.widgets.view.unifiedui.theme

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme

internal data class SnackBarTheme(
    val backgroundColorTheme: ColorTheme?,
    val textColorTheme: ColorTheme?
) : Mergeable<SnackBarTheme> {
    override fun merge(other: SnackBarTheme): SnackBarTheme = SnackBarTheme(
        backgroundColorTheme = backgroundColorTheme merge other.backgroundColorTheme,
        textColorTheme = textColorTheme merge other.textColorTheme
    )
}
