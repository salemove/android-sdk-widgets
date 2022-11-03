package com.glia.widgets.view.unifiedui.config.alert

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal enum class AxisRemoteConfig(val value: String) : Parcelable {
    HORIZONTAL("horizontal"),
    VERTICAL("vertical");

    val isVertical: Boolean
        get() = this == VERTICAL
}