package com.glia.widgets.view.unifiedui.theme.base

import android.graphics.drawable.GradientDrawable
import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeColor(
    val isGradient: Boolean = false, val values: List<Int>
) : Parcelable {
    @get:ColorInt
    val primaryColor: Int
        get() = values.first()

    val valuesArray: IntArray
        get() = values.toIntArray()

    fun asDrawable(): GradientDrawable = if (isGradient)
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, valuesArray)
    else
        GradientDrawable().apply { setColor(primaryColor) }

}
