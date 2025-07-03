package com.glia.widgets.view.unifiedui.theme.callvisulaizer

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge

internal data class CallVisualizerTheme(
    val visitorCodeTheme: VisitorCodeTheme? = null
) : Mergeable<CallVisualizerTheme> {
    override fun merge(other: CallVisualizerTheme): CallVisualizerTheme = CallVisualizerTheme(
        visitorCodeTheme = visitorCodeTheme merge other.visitorCodeTheme
    )
}
