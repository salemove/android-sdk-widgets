package com.glia.widgets.view.unifiedui.config.callvisualizer

import com.glia.widgets.view.unifiedui.theme.callvisulaizer.CallVisualizerTheme
import com.google.gson.annotations.SerializedName

internal data class CallVisualizerConfig(
    @SerializedName("endScreenSharing")
    val endScreenShareRemoteConfig: EndScreenShareRemoteConfig?,
    @SerializedName("visitorCode")
    val visitorCodeRemoteConfig: VisitorCodeRemoteConfig?
) {
    fun toCallVisualizerTheme(): CallVisualizerTheme = CallVisualizerTheme(
        endScreenSharingTheme = endScreenShareRemoteConfig?.toEndScreenShareTheme(),
        visitorCodeTheme = visitorCodeRemoteConfig?.toVisitorCodeTheme()
    )
}
