package com.glia.widgets.view.unifiedui.theme.callvisulaizer

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class VisitorCodeTheme(
    val numberSlotText: TextTheme? = null,
    val numberSlotBackground: LayerTheme? = null,
    val closeButtonColor: ColorTheme? = null,
    val refreshButton: ButtonTheme? = null,
    val background: LayerTheme? = null,
    val title: TextTheme? = null,
    val loadingProgressBar: ColorTheme? = null
) : Mergeable<VisitorCodeTheme> {
    override fun merge(other: VisitorCodeTheme): VisitorCodeTheme = VisitorCodeTheme(
        numberSlotText = numberSlotText merge other.numberSlotText,
        numberSlotBackground = numberSlotBackground merge other.numberSlotBackground,
        closeButtonColor = closeButtonColor merge other.closeButtonColor,
        refreshButton = refreshButton merge other.refreshButton,
        background = background merge other.background,
        title = title merge other.title,
        loadingProgressBar = loadingProgressBar merge other.loadingProgressBar
    )
}
