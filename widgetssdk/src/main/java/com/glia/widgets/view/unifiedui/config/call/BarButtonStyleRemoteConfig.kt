package com.glia.widgets.view.unifiedui.config.call

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class BarButtonStyleRemoteConfig(
    @SerializedName("background")
    val background: ColorLayerRemoteConfig?,

    @SerializedName("imageColor")
    val imageColor: ColorLayerRemoteConfig?,

    @SerializedName("title")
    val title: TextRemoteConfig?
) : Parcelable