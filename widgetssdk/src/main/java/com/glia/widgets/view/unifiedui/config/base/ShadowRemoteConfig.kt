package com.glia.widgets.view.unifiedui.config.base

import androidx.annotation.ColorInt
import com.google.gson.annotations.SerializedName

internal data class ShadowRemoteConfig(
    @SerializedName("color")
    val colorLayerRemoteConfig: ColorLayerRemoteConfig?,

    @SerializedName("offset")
    val elevation: SizeDpRemoteConfig? //using offset param as an elevation, can’t use opacity and radius

) {

    @get:ColorInt
    val color: Int?
        get() = colorLayerRemoteConfig?.primaryColor

    val elevationPx: Float?
        get() = elevation?.valuePx
}