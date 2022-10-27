package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class HeaderRemoteConfig(
    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("backButton")
    val backButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("closeButton")
    val closeButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("endScreenSharingButton")
    val endScreenSharingButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("endButton")
    val endButtonRemoteConfig: ButtonRemoteConfig?
) : Parcelable