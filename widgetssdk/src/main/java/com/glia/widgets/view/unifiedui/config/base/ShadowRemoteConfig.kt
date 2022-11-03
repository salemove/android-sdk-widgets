package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import androidx.annotation.ColorInt
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ShadowRemoteConfig(
    @SerializedName("color")
    val colorLayerRemoteConfig: ColorLayerRemoteConfig?,

    @SerializedName("offset")
    val elevation: SizeDpRemoteConfig? //using offset param as an elevation, can’t use opacity and radius

) : Parcelable {

    @get:ColorInt
    val color: Int?
        get() = colorLayerRemoteConfig?.primaryColor

    val elevationPx: Float?
        get() = elevation?.valuePx
}