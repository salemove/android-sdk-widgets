package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class LayerRemoteConfig(
    @SerializedName("color")
    val color: ColorLayerRemoteConfig?,

    @SerializedName("border")
    val borderColor: ColorLayerRemoteConfig?,

    @SerializedName("borderWidth")
    val borderWidth: SizeDpRemoteConfig?,

    @SerializedName("cornerRadius")
    val cornerRadius: SizeDpRemoteConfig?,
) : Parcelable