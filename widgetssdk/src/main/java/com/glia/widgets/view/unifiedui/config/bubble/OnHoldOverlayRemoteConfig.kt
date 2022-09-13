package com.glia.widgets.view.unifiedui.config.bubble

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.google.gson.annotations.SerializedName

@JvmInline
internal value class OnHoldOverlayRemoteConfig(
    @SerializedName("color")
    val color: ColorLayerRemoteConfig?,
    )
