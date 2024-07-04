package com.glia.widgets.view.unifiedui.theme.base

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge

internal data class HeaderTheme(
    val text: TextTheme? = null,
    val background: LayerTheme? = null,
    val backButton: ButtonTheme? = null,
    val closeButton: ButtonTheme? = null,
    val endButton: ButtonTheme? = null
) : Mergeable<HeaderTheme> {
    override fun merge(other: HeaderTheme): HeaderTheme = HeaderTheme(
        text = text merge other.text,
        background = background merge other.background,
        backButton = backButton merge other.backButton,
        closeButton = closeButton merge other.closeButton,
        endButton = endButton merge other.endButton
    )
}
