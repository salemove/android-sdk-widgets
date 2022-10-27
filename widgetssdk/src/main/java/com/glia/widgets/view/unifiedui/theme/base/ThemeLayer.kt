package com.glia.widgets.view.unifiedui.theme.base

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeLayer(
    val fill: ThemeColor?,
    @ColorInt
    val stroke: Int?, // Currently it is not possible to draw gradient stroke(change to ThemeColor in case of migrating to Jetpack Compose)
    val borderWidth: Float?, // width in pixels
    val cornerRadius: Float? //radius in pixels
) : Parcelable
