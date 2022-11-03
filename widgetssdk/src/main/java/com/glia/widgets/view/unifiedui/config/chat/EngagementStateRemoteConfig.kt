package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class EngagementStateRemoteConfig(
    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("description")
    val description: TextRemoteConfig?,

    @SerializedName("tintColor")
    val tintColor: ColorLayerRemoteConfig?,
): Parcelable
