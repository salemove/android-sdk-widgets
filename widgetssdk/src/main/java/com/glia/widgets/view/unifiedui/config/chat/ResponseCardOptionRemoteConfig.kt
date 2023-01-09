package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.ResponseCardOptionTheme
import com.google.gson.annotations.SerializedName

@JvmInline
internal value class ResponseCardOptionRemoteConfig(
    @SerializedName("normal")
    val normal: ButtonRemoteConfig?
//    No longer needed, because the Response card has only one state when it is open
//    @SerializedName("selected")
//    val selected: ButtonRemoteConfig?,

//    @SerializedName("disabled")
//    val disabled: ButtonRemoteConfig?,
) {
    fun toResponseCardOptionTheme(): ResponseCardOptionTheme = ResponseCardOptionTheme(
        normal = normal?.toButtonTheme(),
    )
}