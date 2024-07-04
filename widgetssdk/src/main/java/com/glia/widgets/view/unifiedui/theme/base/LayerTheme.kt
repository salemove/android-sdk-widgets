package com.glia.widgets.view.unifiedui.theme.base

import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import kotlin.math.roundToInt

internal data class LayerTheme(
    val fill: ColorTheme? = null,
    @ColorInt
    val stroke: Int? = null,
    // Currently, it is impossible to draw a gradient stroke(change to ThemeColor in case of migrating to Jetpack Compose)
    @Px
    val borderWidth: Float? = null, // width in pixels
    @Px
    val cornerRadius: Float? = null // radius in pixels
) : Mergeable<LayerTheme> {

    @get:Px
    val borderWidthInt: Int?
        get() = borderWidth?.roundToInt()

    @get:Px
    val cornerRadiusInt: Int?
        get() = cornerRadius?.roundToInt()

    override fun merge(other: LayerTheme): LayerTheme = LayerTheme(
        fill = fill merge other.fill,
        stroke = stroke merge other.stroke,
        borderWidth = borderWidth merge other.borderWidth,
        cornerRadius = cornerRadius merge other.cornerRadius
    )
}
