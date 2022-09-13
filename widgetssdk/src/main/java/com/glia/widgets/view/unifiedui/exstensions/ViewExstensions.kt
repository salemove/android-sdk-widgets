package com.glia.widgets.view.unifiedui.exstensions

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.glia.widgets.R
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

internal fun View.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(context, resId)
internal fun View.getColorStateListCompat(@ColorRes resId: Int) =
    ContextCompat.getColorStateList(context, resId)

internal fun View.getDrawableCompat(@DrawableRes resId: Int) =
    ContextCompat.getDrawable(context, resId)

internal fun View.getFontCompat(@FontRes resId: Int) = ResourcesCompat.getFont(context, resId)
internal fun Drawable.setTintCompat(@ColorInt color: Int) = DrawableCompat.setTint(this, color)

@AnyRes
internal fun View.getTypedArrayResId(
    typedArray: TypedArray, @StyleableRes index: Int, @AttrRes fallbackAttr: Int
) = Utils.getTypedArrayIntegerValue(typedArray, context, index, fallbackAttr)

internal fun LottieAnimationView.addColorFilter(
    @ColorInt color: Int,
    keyPath: KeyPath = KeyPath("**"),
    mode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP
) = addValueCallback(keyPath, LottieProperty.COLOR_FILTER) {
    PorterDuffColorFilter(color, mode)
}

internal fun ImageView.load(
    url: String?,
    onSuccess: (() -> Unit)? = null,
    onError: ((ex: Exception) -> Unit)? = null
) {

    val callback = if (onSuccess == null && onError == null)
        null
    else
        object : Callback {
            override fun onSuccess() {
                onSuccess?.invoke()
            }

            override fun onError(e: Exception) {
                onError?.invoke(e)
            }
        }

    Picasso.get().load(url).into(this, callback)
}

//Unified Ui
//--------------------------------------------------------------------------------------------------
internal fun View.applyColorTheme(color: ColorTheme?) {
    val isGradient = color?.isGradient ?: return

    if (isGradient) {
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, color.valuesArray)
    } else {
        setBackgroundColor(color.primaryColor)
    }

    backgroundTintList = null
}

/**
 * wii update whole background without keeping old background or stroke
 */
internal fun View.applyLayerTheme(layer: LayerTheme?) {
    if (layer?.fill == null && layer?.stroke == null) return

    val drawable = (background as? GradientDrawable) ?: GradientDrawable()

    if (backgroundTintList != null) {
        drawable.color = backgroundTintList
        backgroundTintList = null
    }

    layer.fill?.also {
        if (it.isGradient) {
            drawable.colors = it.valuesArray
        } else {
            drawable.setColor(it.primaryColor)
        }
    }

    layer.stroke?.also {
        drawable.setStroke(
            layer.borderWidthInt ?: context.resources.getDimensionPixelSize(R.dimen.glia_px), it
        )
    }

    layer.cornerRadius?.also { drawable.cornerRadius = it }

    background = drawable
}

internal fun MaterialCardView.applyCardLayerTheme(layer: LayerTheme?) {
    layer?.fill?.primaryColor?.also(::setCardBackgroundColor)
    layer?.stroke?.also(::setStrokeColor)
    layer?.borderWidthInt?.also(::setStrokeWidth)
    layer?.cornerRadius?.also(::setRadius)
}

internal fun View.applyShadow(@ColorInt color: Int?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && color != null) {
        outlineSpotShadowColor = color
        outlineAmbientShadowColor = color
    }
}

internal fun TextView.applyTextColorTheme(color: ColorTheme?) {
    if (color == null) return

    if (color.isGradient) {
        paint.shader = LinearGradient(
            0f,
            0f,
            paint.measureText(text.toString()),
            textSize,
            color.valuesArray,
            null,
            Shader.TileMode.CLAMP
        )
    } else {
        setTextColor(color.primaryColor)
    }

}

internal fun TextView.applyTextTheme(textTheme: TextTheme?, withBackground: Boolean = false) {
    textTheme?.textColor.also(::applyTextColorTheme)
    textTheme?.textSize?.also { setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
    textTheme?.textStyle?.also { typeface = Typeface.create(typeface, it) }
    textTheme?.textAlignment?.let { textAlignment = it }

    if (withBackground) {
        textTheme?.backgroundColor.also(::applyColorTheme)
    }
}

internal fun MaterialButton.applyButtonTheme(buttonTheme: ButtonTheme?) {
    buttonTheme?.background?.also { bg ->
        bg.fill?.also { backgroundTintList = it.primaryColorStateList }
        bg.stroke?.also { strokeColor = ColorStateList.valueOf(it) }
        bg.cornerRadiusInt?.also { cornerRadius = it }
        bg.borderWidthInt?.also { strokeWidth = it }
    }

    buttonTheme?.elevation?.also { elevation = it }
    buttonTheme?.shadowColor?.also(::applyShadow)
    buttonTheme?.text.also(::applyTextTheme)
}

internal fun ImageView.applyButtonTheme(buttonTheme: ButtonTheme?) {
    applyImageColorTheme(buttonTheme?.iconColor)
}

internal fun ImageView.applyImageColorTheme(colorTheme: ColorTheme?) {
    colorTheme?.primaryColorStateList.also(::setImageTintList)
}
