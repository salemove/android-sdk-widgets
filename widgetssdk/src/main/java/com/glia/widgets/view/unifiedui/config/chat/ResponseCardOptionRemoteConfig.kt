package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.ResponseCardOptionTheme
import com.google.gson.annotations.SerializedName

internal data class ResponseCardOptionRemoteConfig(
    @SerializedName("normal")
    val normal: ButtonRemoteConfig?,

    @SerializedName("selected")
    val selected: ButtonRemoteConfig?,

    @SerializedName("disabled")
    val disabled: ButtonRemoteConfig?,
) {
    fun toResponseCardOptionTheme(): ResponseCardOptionTheme = ResponseCardOptionTheme(
        normal = normal?.toButtonTheme(),
        disabled = disabled?.toButtonTheme(),
        selected = selected?.toButtonTheme(),
    )
}