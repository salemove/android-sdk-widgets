package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class UpgradeRemoteConfig(

    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("description")
    val description: TextRemoteConfig?,

    @SerializedName("iconColor")
    val iconColor: ColorLayerRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,
): Parcelable
