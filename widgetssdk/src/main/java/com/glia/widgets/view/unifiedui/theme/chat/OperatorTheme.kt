package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme

internal data class OperatorTheme(
    val image: UserImageTheme? = null,
    val animationColor: ColorTheme? = null,
    val onHoldOverlay: OnHoldOverlayTheme? = null
) : Mergeable<OperatorTheme> {
    override fun merge(other: OperatorTheme): OperatorTheme = OperatorTheme(
        image = image merge other.image,
        animationColor = animationColor merge other.animationColor,
        onHoldOverlay = onHoldOverlay merge other.onHoldOverlay
    )
}
