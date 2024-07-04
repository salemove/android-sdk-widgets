package com.glia.widgets.view.unifiedui.theme.callvisulaizer

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class EndScreenSharingTheme(
    val header: HeaderTheme? = null,
    val endButton: ButtonTheme? = null,
    val label: TextTheme? = null,
    val background: LayerTheme? = null
) : Mergeable<EndScreenSharingTheme> {
    override fun merge(other: EndScreenSharingTheme): EndScreenSharingTheme = EndScreenSharingTheme(
        header = header merge other.header,
        endButton = endButton merge other.endButton,
        label = label merge other.label,
        background = background merge other.background
    )
}
