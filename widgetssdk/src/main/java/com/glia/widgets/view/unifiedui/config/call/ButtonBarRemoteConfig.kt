package com.glia.widgets.view.unifiedui.config.call

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable