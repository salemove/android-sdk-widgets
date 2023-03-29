package com.glia.widgets.view.unifiedui.config.base

import com.google.gson.annotations.SerializedName

internal data class FontRemoteConfig(
    @SerializedName("size")
    val size: SizeSpRemoteConfig?,

    @SerializedName("style")
    val style: TextStyleRemoteConfig?
)