package com.glia.widgets.view.unifiedui.config.call

import com.glia.widgets.view.unifiedui.theme.call.BarButtonStatesTheme
import com.google.gson.annotations.SerializedName

internal data class BarButtonStatesRemoteConfig(
    @SerializedName("inactive")
    val disabled: BarButtonStyleRemoteConfig?,

    @SerializedName("active")
    val enabled: BarButtonStyleRemoteConfig?,

    @SerializedName("selected")
    val activated: BarButtonStyleRemoteConfig?
) {
    fun toBarButtonStatesTheme(): BarButtonStatesTheme = BarButtonStatesTheme(
        disabled = disabled?.toBarButtonTheme(),
        enabled = enabled?.toBarButtonTheme(),
        activated = activated?.toBarButtonTheme()
    )
}