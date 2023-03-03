package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import androidx.annotation.ColorInt
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import kotlinx.parcelize.Parcelize

/**
 * Represents ARGB color fields from remote config e.g. #FF44DD55
 * with the help of [com.glia.widgets.view.unifiedui.parse.ColorDeserializer] it guarantees
 * that [color] will return parsed valid color value
 */
@JvmInline
@Parcelize
internal value class ColorRemoteConfig(@ColorInt val color: Int) : Parcelable {
    fun toColorTheme() = ColorTheme(color)
}