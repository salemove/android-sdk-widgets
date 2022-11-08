package com.glia.widgets.view.unifiedui.theme.base

import android.graphics.drawable.GradientDrawable
import android.os.Parcelable
import androidx.annotation.ColorInt
import com.glia.widgets.di.Dependencies
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

@Parcelize
internal data class ThemeLayer(
    val fill: ThemeColor? = null,
    @ColorInt
    val stroke: Int? = null, // Currently it is not possible to draw gradient stroke(change to ThemeColor in case of migrating to Jetpack Compose)
    val borderWidth: Float? = null, // width in pixels
    val cornerRadius: Float? = null //radius in pixels
) : Parcelable {

    fun asDrawable(): GradientDrawable? = takeIf { it.fill != null && it.stroke != null }?.run {

        val drawable: GradientDrawable = fill?.asDrawable() ?: GradientDrawable()

        stroke?.also {
            val borderWidth = borderWidth?.roundToInt() ?: Dependencies.getResourceProvider()
                .convertDpToPixel(1f)
                .roundToInt()

            drawable.setStroke(borderWidth, it)
        }

        cornerRadius?.also { drawable.cornerRadius = it }

        drawable
    }
}
