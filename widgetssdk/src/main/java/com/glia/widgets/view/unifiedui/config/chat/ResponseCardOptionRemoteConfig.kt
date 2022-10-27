package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ResponseCardOptionRemoteConfig(
    @SerializedName("normal")
    val normal: ButtonRemoteConfig?,

    @SerializedName("selected")
    val selected: ButtonRemoteConfig?,

    @SerializedName("disabled")
    val disabled: ButtonRemoteConfig?,
): Parcelable
