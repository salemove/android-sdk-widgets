package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class FontRemoteConfig(
    @SerializedName("size")
    val size: SizeSpRemoteConfig?,

    @SerializedName("style")
    val style: TextStyleRemoteConfig?
) : Parcelable