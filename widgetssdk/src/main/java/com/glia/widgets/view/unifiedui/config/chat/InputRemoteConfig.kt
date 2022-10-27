package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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
): Parcelable
