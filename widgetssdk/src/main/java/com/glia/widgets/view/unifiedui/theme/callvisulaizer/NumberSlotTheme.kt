package com.glia.widgets.view.unifiedui.theme.callvisulaizer

import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class NumberSlotTheme(
    val textColor: ColorTheme?,
    val textSize: Float?, // Size in SP
    val textStyle: Int?, //Typeface.NORMAL
    val background: LayerTheme?
) {
    fun toTextTheme(): TextTheme {
        return TextTheme(textColor, background?.fill, textSize, textStyle, null)
    }
}
