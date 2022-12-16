package com.glia.widgets.view.unifiedui.exstensions

import android.content.res.ColorStateList
import androidx.annotation.ColorInt

@ColorInt
fun ColorStateList?.colorForStateOrNull(state: IntArray?): Int? =
    this?.getColorForState(state, 0)?.takeIf { it > 0 }