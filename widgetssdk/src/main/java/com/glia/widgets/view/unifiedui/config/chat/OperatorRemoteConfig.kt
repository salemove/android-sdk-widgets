package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class OperatorRemoteConfig(
    @SerializedName("image")
    val image: UserImageRemoteConfig?,

    @SerializedName("animationColor")
    val animationColor: ColorLayerRemoteConfig?,

    @SerializedName("overlayColor")
    val overlayColor: ColorLayerRemoteConfig?
): Parcelable
