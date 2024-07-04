package com.glia.widgets.view.unifiedui.theme.call

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class BarButtonStyleTheme(
    val background: ColorTheme? = null,
    val imageColor: ColorTheme? = null,
    val title: TextTheme? = null
) : Mergeable<BarButtonStyleTheme> {
    override fun merge(other: BarButtonStyleTheme): BarButtonStyleTheme = BarButtonStyleTheme(
        background = background merge other.background,
        imageColor = imageColor merge other.imageColor,
        title = title merge other.title
    )
}
