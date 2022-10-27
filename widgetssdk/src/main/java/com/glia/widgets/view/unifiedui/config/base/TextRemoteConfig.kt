package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class TextRemoteConfig(
    @SerializedName("foreground")
    val textColor: ColorLayerRemoteConfig?,

    @SerializedName("background")
    val backgroundColor: ColorLayerRemoteConfig?,

    @SerializedName("font")
    val fontRemoteConfig: FontRemoteConfig?,

    @SerializedName("alignment")
    val alignment: AlignmentTypeRemoteConfig?,
) : Parcelable {
    val fontSize: Float?
        get() = fontRemoteConfig?.size?.value

    val fontStyle: Int?
        get() = fontRemoteConfig?.style?.style

    val nativeAlignment: Int?
        get() = alignment?.nativeAlignment

}