package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents ARGB color fields from remote config e.g. #FF44DD55
 * with the help of [com.glia.widgets.view.unifiedui.parse.ColorDeserializer] it guarantees
 * that [color] will return parsed valid color value
 */
@JvmInline
@Parcelize
internal value class ColorRemoteConfig(val color: Int): Parcelable