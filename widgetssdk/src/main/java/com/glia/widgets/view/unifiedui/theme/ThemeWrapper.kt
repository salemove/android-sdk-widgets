package com.glia.widgets.view.unifiedui.theme

import android.graphics.Typeface
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme

internal interface TypefaceThemeWrapper<T : Any> {
    val theme: T
    val typeface: Typeface?
}

internal data class AlertThemeWrapper(
    override val theme: AlertTheme,
    override val typeface: Typeface?
) : TypefaceThemeWrapper<AlertTheme>
