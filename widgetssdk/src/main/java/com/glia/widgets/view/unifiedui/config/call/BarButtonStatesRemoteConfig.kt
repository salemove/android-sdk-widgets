package com.glia.widgets.view.unifiedui.config.call

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class BarButtonStatesRemoteConfig(
    @SerializedName("inactive")
    val inactive: BarButtonStyleRemoteConfig?,

    @SerializedName("active")
    val active: BarButtonStyleRemoteConfig?,

    @SerializedName("selected")
    val selected: BarButtonStyleRemoteConfig?
) : Parcelable