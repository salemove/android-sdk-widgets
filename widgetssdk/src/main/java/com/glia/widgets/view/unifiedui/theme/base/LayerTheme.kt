package com.glia.widgets.view.unifiedui.theme.base

import androidx.annotation.ColorInt
import androidx.annotation.Px
import kotlin.math.roundToInt

internal data class LayerTheme(
    val fill: ColorTheme? = null,
    @ColorInt
    val stroke: Int? = null,
    // Currently it is not possible to draw gradient stroke(change to ThemeColor in case of migrating to Jetpack Compose)
    @Px
    val borderWidth: Float? = null, // width in pixels
    @Px
    val cornerRadius: Float? = null // radius in pixels
) {

    @get:Px
    val borderWidthInt: Int?
        get() = borderWidth?.roundToInt()

    @get:Px
    val cornerRadiusInt: Int?
        get() = cornerRadius?.roundToInt()
}
