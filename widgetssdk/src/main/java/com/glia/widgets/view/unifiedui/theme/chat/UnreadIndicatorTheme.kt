package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme

internal data class UnreadIndicatorTheme(
    val background: ColorTheme? = null,
    val bubble: BubbleTheme? = null
) : Mergeable<UnreadIndicatorTheme> {
    override fun merge(other: UnreadIndicatorTheme): UnreadIndicatorTheme = UnreadIndicatorTheme(
        background = background merge other.background,
        bubble = bubble merge other.bubble
    )
}
