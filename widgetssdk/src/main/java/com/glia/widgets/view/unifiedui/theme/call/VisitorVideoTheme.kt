package com.glia.widgets.view.unifiedui.theme.call

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge

@JvmInline
internal value class VisitorVideoTheme(
    val flipCameraButton: BarButtonStyleTheme? = null
) : Mergeable<VisitorVideoTheme> {
    override fun merge(other: VisitorVideoTheme): VisitorVideoTheme = VisitorVideoTheme(flipCameraButton merge other.flipCameraButton)
}
