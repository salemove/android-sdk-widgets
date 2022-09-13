package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.InputTheme
import com.google.gson.annotations.SerializedName

internal data class InputRemoteConfig(
    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("placeholder")
    val placeholder: TextRemoteConfig?,

    @SerializedName("separator")
    val separator: ColorLayerRemoteConfig?,

    @SerializedName("sendButton")
    val sendButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("mediaButton")
    val mediaButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("fileUploadBar")
    val fileUploadBarRemoteConfig: FileUploadBarRemoteConfig?
) {
    fun toInputTheme(): InputTheme = InputTheme(
        text = textRemoteConfig?.toTextTheme(),
        placeholder = placeholder?.toTextTheme(),
        divider = separator?.toColorTheme(),
        sendButton = sendButtonRemoteConfig?.toButtonTheme(),
        mediaButton = mediaButtonRemoteConfig?.toButtonTheme(),
        background = background?.toLayerTheme(),
        fileUploadBar = fileUploadBarRemoteConfig?.toFileUploadBarTheme()
    )
}
