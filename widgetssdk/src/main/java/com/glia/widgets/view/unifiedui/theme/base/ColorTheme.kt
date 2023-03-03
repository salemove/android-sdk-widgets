package com.glia.widgets.view.unifiedui.theme.base

import android.content.res.ColorStateList
import androidx.annotation.ColorInt

internal data class ColorTheme(
    val isGradient: Boolean = false, val values: List<Int>
) {
    internal constructor(@ColorInt color: Int) : this(values = listOf(color))

    @get:ColorInt
    val primaryColor: Int
        get() = values.first()

    val valuesArray: IntArray
        get() = values.toIntArray()

    val primaryColorStateList: ColorStateList
        get() = ColorStateList.valueOf(primaryColor)

}
