package com.glia.widgets.view.unifiedui.theme.base

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge

internal data class TextInputTheme(
    val textTheme: TextTheme?,
    val backgroundTheme: LayerTheme?
) : Mergeable<TextInputTheme> {
    override fun merge(other: TextInputTheme): TextInputTheme = TextInputTheme(
        textTheme = textTheme merge other.textTheme,
        backgroundTheme = backgroundTheme merge other.backgroundTheme
    )
}
