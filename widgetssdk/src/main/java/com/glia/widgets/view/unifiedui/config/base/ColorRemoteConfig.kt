package com.glia.widgets.view.unifiedui.config.base

import androidx.annotation.ColorInt
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme

/**
 * Represents ARGB color fields from remote config e.g. #FF44DD55
 * with the help of [com.glia.widgets.view.unifiedui.parse.ColorDeserializer] it guarantees
 * that [color] will return parsed valid color value
 */
@JvmInline
internal value class ColorRemoteConfig(@ColorInt val color: Int) {
    fun toColorTheme() = ColorTheme(color)
}
