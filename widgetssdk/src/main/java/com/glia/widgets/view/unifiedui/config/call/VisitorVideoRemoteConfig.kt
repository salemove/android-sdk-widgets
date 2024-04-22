package com.glia.widgets.view.unifiedui.config.call

import com.glia.widgets.view.unifiedui.theme.call.VisitorVideoTheme
import com.google.gson.annotations.SerializedName

internal data class VisitorVideoRemoteConfig(
    @SerializedName("flipCameraButton")
    val flipCameraButton: BarButtonStyleRemoteConfig?
) {
    fun toVisitorVideoTheme(): VisitorVideoTheme = VisitorVideoTheme(
        flipCameraButton = flipCameraButton?.toBarButtonTheme()
    )
}
