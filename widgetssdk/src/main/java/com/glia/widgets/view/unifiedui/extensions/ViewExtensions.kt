package com.glia.widgets.view.unifiedui.extensions

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.glia.widgets.view.button.GliaSurveyOptionButton
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.call.BarButtonStatesTheme
import com.glia.widgets.view.unifiedui.theme.survey.OptionButtonTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

@AnyRes
internal fun View.getAttr(@AttrRes attr: Int, @AnyRes fallBackResId: Int): Int = TypedValue().takeIf {
    context.theme.resolveAttribute(attr, it, true)
}?.resourceId ?: fallBackResId

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

internal val View.layoutInflater: LayoutInflater get() = LayoutInflater.from(this.context)

//Unified Ui
//--------------------------------------------------------------------------------------------------
internal fun View.applyColorTheme(color: ColorTheme?) {
    background = createBackgroundFromTheme(color ?: return)
    backgroundTintList = null
}

internal fun createBackgroundFromTheme(color: ColorTheme): Drawable {
    return if (color.isGradient) {
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, color.valuesArray)
    } else {
        ColorDrawable(color.primaryColor)
    }
}

/**
 * wii update whole background without keeping old background or stroke
 */
internal fun View.applyLayerTheme(layer: LayerTheme?, padding: Rect? = null) {
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

    background = padding?.let {
        LayerDrawable(arrayOf(drawable)).apply {
            setPadding(padding.left, padding.top, padding.right, padding.bottom)
        }
    } ?: drawable
}

internal fun View.applyLayerTheme(@ColorInt backgroundColor: Int?) {
    backgroundColor?.apply(::setBackgroundColor)
}

internal fun MaterialCardView.applyCardLayerTheme(layer: LayerTheme?) {
    layer?.fill?.primaryColor?.also {
        /*
        when the card has opacity/alpha, then shadow shapes inner part becomes visible,
        so in this case we need to make elevation = 0 to prevent this behavior
        */
        if (Color.alpha(it) > 0) cardElevation = 0f
    }
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

internal fun TextView.applyTextTheme(
    textTheme: TextTheme?, withBackground: Boolean = false, withAlignment: Boolean = true
) {
    textTheme?.textColor.also(::applyTextColorTheme)
    textTheme?.textSize?.also { setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
    textTheme?.textStyle?.also { typeface = Typeface.create(typeface, it) }

    if (withAlignment)
        textTheme?.textAlignment?.let { textAlignment = it }

    if (withBackground) {
        textTheme?.backgroundColor.also(::applyColorTheme)
    }
}

internal fun TextView.applyTextTheme(
    @ColorInt textColor: Int?,
    textFont: Typeface?
) {
    textColor?.also(::setTextColor)
    textFont?.also(::setTypeface)
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
    buttonTheme?.text?.also {
        applyTextTheme(it)
        // Also apply text color to button icon
        iconTint = it.textColor?.primaryColorStateList
    }
}

internal fun MaterialButton.applyButtonTheme(
    @ColorInt backgroundColor: Int?,
    @ColorInt textColor: Int?,
    textFont: Typeface?
) {
    backgroundColor?.also { backgroundTintList = ColorStateList.valueOf(it) }
    textColor?.also {
        setTextColor(it)
        iconTint = ColorStateList.valueOf(it)
    }
    textFont?.also(::setTypeface)
}

internal fun ImageView.applyButtonTheme(buttonTheme: ButtonTheme?) {
    applyImageColorTheme(buttonTheme?.iconColor)
}

internal fun View.changeStatusBarColor(color: Int?) {
    color?.run {
        Utils.getActivity(context)?.window?.statusBarColor = this
    }
}

internal fun ProgressBar.applyProgressColorTheme(colorTheme: ColorTheme?) {
    colorTheme?.primaryColorStateList?.also(::setIndeterminateTintList)
}

internal fun ProgressBar.applyProgressColorTheme(@ColorInt progressColor: Int?) {
    progressColor?.also {
        indeterminateTintList = ColorStateList.valueOf(it)
    }
}

internal fun ImageView.applyImageColorTheme(colorTheme: ColorTheme?) {
    colorTheme?.primaryColorStateList?.also(::setImageTintList)
}

internal fun ImageView.applyImageColorTheme(@ColorInt imageColor: Int?) {
    imageColor?.let {
        imageTintList = ColorStateList.valueOf(it)
    }
}

internal fun FloatingActionButton.applyBarButtonStatesTheme(barButtonStatesTheme: BarButtonStatesTheme?) {
    if (barButtonStatesTheme == null) return

    val colors: MutableList<Int> = mutableListOf()
    val states: MutableList<IntArray> = mutableListOf()

    val disabledState = intArrayOf(-android.R.attr.state_enabled)
    val activatedState = intArrayOf(android.R.attr.state_activated)
    val enabledState = intArrayOf()

    val oldBgTint = supportBackgroundTintList

    val bgDisabledColor = barButtonStatesTheme.disabled?.background?.primaryColor
        ?: oldBgTint.colorForStateOrNull(disabledState)

    if (bgDisabledColor != null) {
        colors.add(bgDisabledColor)
        states.add(disabledState)
    }

    val bgActivatedColor = barButtonStatesTheme.activated?.background?.primaryColor
        ?: oldBgTint.colorForStateOrNull(activatedState)

    if (bgActivatedColor != null) {
        colors.add(bgActivatedColor)
        states.add(activatedState)
    }

    val bgEnabledColor = barButtonStatesTheme.enabled?.background?.primaryColor
        ?: oldBgTint.colorForStateOrNull(enabledState)

//        Default value must be at the latest position
    if (bgEnabledColor != null) {
        colors.add(bgEnabledColor)
        states.add(enabledState)
    }

    supportBackgroundTintList = ColorStateList(states.toTypedArray(), colors.toIntArray())
    states.clear()
    colors.clear()

    val oldImageTintList = supportImageTintList

    val disabledColor = barButtonStatesTheme.disabled?.imageColor?.primaryColor
        ?: oldImageTintList.colorForStateOrNull(disabledState)

    if (disabledColor != null) {
        colors.add(disabledColor)
        states.add(disabledState)
    }

    val activatedColor = barButtonStatesTheme.activated?.imageColor?.primaryColor
        ?: oldImageTintList.colorForStateOrNull(activatedState)

    if (activatedColor != null) {
        colors.add(activatedColor)
        states.add(activatedState)
    }

    val enabledColor = barButtonStatesTheme.enabled?.imageColor?.primaryColor
        ?: oldImageTintList.colorForStateOrNull(enabledState)

    if (enabledColor != null) {
        colors.add(enabledColor)
        states.add(enabledState)
    }

    supportImageTintList = ColorStateList(states.toTypedArray(), colors.toIntArray())
    states.clear()
    colors.clear()
}

internal fun GliaSurveyOptionButton.applyOptionButtonTheme(theme: OptionButtonTheme?) {
    val textTheme: TextTheme?
    val layerTheme: LayerTheme?
    when {
        isError -> {
            textTheme = theme?.highlightedText
            layerTheme = theme?.highlightedLayer
        }
        isSelected -> {
            textTheme = theme?.selectedText
            layerTheme = theme?.selectedLayer
        }
        else -> {
            textTheme = theme?.normalText
            layerTheme = theme?.normalLayer
        }
    }
    applyButtonTheme(
        ButtonTheme(
            textTheme?.copy(textSize = theme?.fontSize, textStyle = theme?.fontStyle),
            layerTheme, null, null, null
        )
    )
}
