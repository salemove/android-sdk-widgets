package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme

internal data class OnHoldOverlayTheme(
    val backgroundColor: ColorTheme? = null,
    val tintColor: ColorTheme? = null
) : Mergeable<OnHoldOverlayTheme> {
    override fun merge(other: OnHoldOverlayTheme): OnHoldOverlayTheme = OnHoldOverlayTheme(
        backgroundColor = backgroundColor merge other.backgroundColor,
        tintColor = tintColor merge other.tintColor
    )
}
