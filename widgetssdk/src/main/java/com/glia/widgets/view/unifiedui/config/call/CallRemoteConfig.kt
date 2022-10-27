package com.glia.widgets.view.unifiedui.config.call

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.HeaderRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CallRemoteConfig(
    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("bottomText")
    val bottomTextRemoteConfig: TextRemoteConfig?,

    @SerializedName("buttonBar")
    val buttonBarRemoteConfig: ButtonBarRemoteConfig?,

    @SerializedName("duration")
    val duration: TextRemoteConfig?,

    @SerializedName("endButton")
    val endButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("header")
    val headerRemoteConfig: HeaderRemoteConfig?,

    @SerializedName("operator")
    val operator: TextRemoteConfig?,

    @SerializedName("topText")
    val topTextRemoteConfig: TextRemoteConfig?
) : Parcelable