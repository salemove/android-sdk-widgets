package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt


/**
 * Represents size in Sp (Scalable pixel)
 * Used by deserializer to differ sp from dp
 *
 * @see [com.glia.widgets.view.unifiedui.parse.SpDeserializer]
 */
@Parcelize
@JvmInline
internal value class SizeSpRemoteConfig(val value: Float) : Parcelable {
    val intValue: Int
        get() = value.roundToInt()
}

/**
 * Represents size in Dp (Density independent pixel)
 * Used by deserializer to differ sp from dp
 *
 * @see [com.glia.widgets.view.unifiedui.parse.DpDeserializer]
 */
@Parcelize
@JvmInline
internal value class SizeDpRemoteConfig(val valuePx: Float) : Parcelable {
    val intValuePx: Int
        get() = valuePx.roundToInt()
}