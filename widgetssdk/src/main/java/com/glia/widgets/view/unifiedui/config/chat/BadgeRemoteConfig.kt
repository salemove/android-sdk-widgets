package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.FontRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class BadgeRemoteConfig(
    @SerializedName("font")
    val fontRemoteConfig: FontRemoteConfig?,

    @SerializedName("fontColor")
    val fontColor: ColorLayerRemoteConfig?,

    @SerializedName("backgroundColor")
    val backgroundColor: ColorLayerRemoteConfig?,
) : Parcelable {
    val fontSize: Float?
        get() = fontRemoteConfig?.size?.value

    val fontStyle: Int?
        get() = fontRemoteConfig?.style?.style
}
