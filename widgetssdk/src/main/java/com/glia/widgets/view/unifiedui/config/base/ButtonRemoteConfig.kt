package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import androidx.annotation.ColorInt
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ButtonRemoteConfig(
    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("tintColor")
    val tintColor: ColorLayerRemoteConfig?,

    @SerializedName("shadow")
    val shadowRemoteConfig: ShadowRemoteConfig?
) : Parcelable {

    @get:ColorInt
    val iconColor: Int?
        get() = tintColor?.primaryColor

    val elevation: Float?
        get() = shadowRemoteConfig?.elevationPx

    @get:ColorInt
    val shadowColor: Int?
        get() = shadowRemoteConfig?.color
}