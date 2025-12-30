package com.glia.widgets.view.unifiedui.theme.base

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

internal data class ColorTheme(
    val isGradient: Boolean = false,
    val values: List<Int>
) {
    constructor(@ColorInt color: Int) : this(values = listOf(color))

    constructor() : this(Color.TRANSPARENT)

    @get:ColorInt
    val primaryColor: Int
        get() = values.first()

    val valuesArray: IntArray
        get() = values.toIntArray()

    val primaryColorStateList: ColorStateList
        get() = ColorStateList.valueOf(primaryColor)

    fun withAlpha(@androidx.annotation.FloatRange(from = 0.0, to = 100.0) alpha: Float): ColorTheme {
        val realAlpha: Float = alpha * 255 / 100
        val newColors = values.map { ColorUtils.setAlphaComponent(it, realAlpha.roundToInt()) }

        return copy(values = newColors)
    }
}
