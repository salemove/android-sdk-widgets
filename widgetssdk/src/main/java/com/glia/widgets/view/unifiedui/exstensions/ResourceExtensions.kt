package com.glia.widgets.view.unifiedui.exstensions

import android.content.Context
import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes

@ColorInt
fun ColorStateList?.colorForState(state: IntArray): Int? =
    this?.getColorForState(state, defaultColor)

fun Context.getDimenRes(@DimenRes dimenId: Int): Float {
    return this.resources.getDimension(dimenId)
}

fun Context.getDimenResPx(@DimenRes dimenId: Int): Int {
    return this.resources.getDimensionPixelSize(dimenId)
}

fun String.separateStringWithSymbol(symbol: String): String {
    return this.split("").joinToString(symbol)
}
