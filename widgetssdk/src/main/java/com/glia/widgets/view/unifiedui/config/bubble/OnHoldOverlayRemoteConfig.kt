package com.glia.widgets.view.unifiedui.config.bubble

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
internal value class OnHoldOverlayRemoteConfig(
    @SerializedName("color")
    val color: ColorLayerRemoteConfig?,
    ) : Parcelable
