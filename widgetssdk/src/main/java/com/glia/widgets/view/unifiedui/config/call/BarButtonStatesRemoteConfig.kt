package com.glia.widgets.view.unifiedui.config.call

import com.glia.widgets.view.unifiedui.theme.call.BarButtonStatesTheme
import com.google.gson.annotations.SerializedName

internal data class BarButtonStatesRemoteConfig(
    @SerializedName("inactive")
    val inactive: BarButtonStyleRemoteConfig?,

    @SerializedName("active")
    val active: BarButtonStyleRemoteConfig?,

    @SerializedName("selected")
    val selected: BarButtonStyleRemoteConfig?
) {
    fun toBarButtonStatesTheme(): BarButtonStatesTheme = BarButtonStatesTheme(
        inactive = inactive?.toBarButtonTheme(),
        active = active?.toBarButtonTheme(),
        selected = selected?.toBarButtonTheme()
    )
}