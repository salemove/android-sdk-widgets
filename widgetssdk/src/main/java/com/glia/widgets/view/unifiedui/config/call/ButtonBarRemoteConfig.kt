package com.glia.widgets.view.unifiedui.config.call

import com.glia.widgets.view.unifiedui.theme.call.ButtonBarTheme
import com.google.gson.annotations.SerializedName

internal data class ButtonBarRemoteConfig(
    @SerializedName("chatButton")
    val chatButton: BarButtonStatesRemoteConfig?,

    @SerializedName("minimizeButton")
    val minimizeButton: BarButtonStatesRemoteConfig?,

    @SerializedName("muteButton")
    val muteButton: BarButtonStatesRemoteConfig?,

    @SerializedName("speakerButton")
    val speakerButton: BarButtonStatesRemoteConfig?,

    @SerializedName("videoButton")
    val videoButton: BarButtonStatesRemoteConfig?
) {
    fun toButtonBarTheme(): ButtonBarTheme = ButtonBarTheme(
        chatButton = chatButton?.toBarButtonStatesTheme(),
        minimizeButton = minimizeButton?.toBarButtonStatesTheme(),
        muteButton = muteButton?.toBarButtonStatesTheme(),
        speakerButton = speakerButton?.toBarButtonStatesTheme(),
        videoButton = videoButton?.toBarButtonStatesTheme()
    )
}