package com.glia.widgets.view.unifiedui.config.callvisualizer

import com.glia.widgets.view.unifiedui.theme.callvisulaizer.CallVisualizerTheme
import com.google.gson.annotations.SerializedName

internal data class CallVisualizerConfig(

    @SerializedName("visitorCode")
    val visitorCodeRemoteConfig: VisitorCodeRemoteConfig?
) {
    fun toCallVisualizerTheme(): CallVisualizerTheme = CallVisualizerTheme(
        visitorCodeTheme = visitorCodeRemoteConfig?.toVisitorCodeTheme()
    )
}
