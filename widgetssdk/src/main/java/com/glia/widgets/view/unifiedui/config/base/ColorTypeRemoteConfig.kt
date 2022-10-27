package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal enum class ColorTypeRemoteConfig(val type: String): Parcelable {
    FILL("fill"),
    GRADIENT("gradient")
}