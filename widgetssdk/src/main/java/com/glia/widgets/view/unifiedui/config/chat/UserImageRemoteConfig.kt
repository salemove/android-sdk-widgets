package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class UserImageRemoteConfig(
    @SerializedName("placeholderColor")
    val placeholderColor: ColorLayerRemoteConfig?,

    @SerializedName("placeholderBackgroundColor")
    val placeholderBackgroundColor: ColorLayerRemoteConfig?,

    @SerializedName("imageBackgroundColor")
    val imageBackgroundColor: ColorLayerRemoteConfig?,
): Parcelable
