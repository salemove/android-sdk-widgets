package com.glia.widgets.view.unifiedui.config.base

import kotlin.math.roundToInt


/**
 * Represents size in Sp (Scalable pixel)
 * Used by deserializer to differ sp from dp
 *
 * @see [com.glia.widgets.view.unifiedui.parse.SpDeserializer]
 */
@JvmInline
internal value class SizeSpRemoteConfig(val value: Float) {
    val intValue: Int
        get() = value.roundToInt()
}

/**
 * Represents size in Dp (Density independent pixel)
 * Used by deserializer to differ sp from dp
 *
 * @see [com.glia.widgets.view.unifiedui.parse.DpDeserializer]
 */
@JvmInline
internal value class SizeDpRemoteConfig(val valuePx: Float) {
    val intValuePx: Int
        get() = valuePx.roundToInt()
}