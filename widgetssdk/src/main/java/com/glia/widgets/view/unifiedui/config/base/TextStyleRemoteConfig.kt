package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents text style from remote config
 * @param style is [android.graphics.Typeface] style.
 */
@JvmInline
@Parcelize
internal value class TextStyleRemoteConfig(val style: Int) : Parcelable