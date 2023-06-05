package com.glia.widgets.view.unifiedui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.glia.widgets.R
import com.glia.widgets.helper.applyShadow
import com.glia.widgets.helper.colorForState
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
import com.google.android.material.progressindicator.CircularProgressIndicator

internal fun View.applyColorTheme(color: ColorTheme?) {
    background = createBackgroundFromTheme(color ?: return)
    backgroundTintList = null
}

internal fun createBackgroundFromTheme(color: ColorTheme): Drawable = color.run {
    if (isGradient) {
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, valuesArray)
    } else {
        GradientDrawable().apply { setColor(primaryColor) }
    }
}

/**
 * will update whole background without keeping an old background or stroke
 * in case when the old background differs from [GradientDrawable]
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
            layer.borderWidthInt ?: context.resources.getDimensionPixelSize(R.dimen.glia_px),
            it
        )
    }

    layer.cornerRadius?.also { drawable.cornerRadius = it }

    background = drawable
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
    textTheme: TextTheme?,
    withBackground: Boolean = false,
    withAlignment: Boolean = true
) {
    textTheme?.textColor.also(::applyTextColorTheme)
    textTheme?.textSize?.also { setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
    textTheme?.textStyle?.also { typeface = Typeface.create(typeface, it) }

    if (withAlignment) {
        textTheme?.textAlignment?.let { textAlignment = it }
    }
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
    buttonTheme?.text?.also {
        applyTextTheme(it)
    }

    // If no `iconColor` provided use text color
    buttonTheme?.let { it.iconColor ?: it.text?.textColor }?.primaryColorStateList?.also {
        iconTint = it
    }
}

internal fun ImageView.applyButtonTheme(buttonTheme: ButtonTheme?) {
    applyImageColorTheme(buttonTheme?.iconColor)
}

internal fun CircularProgressIndicator.applyIndicatorColorTheme(colorTheme: ColorTheme?) {
    colorTheme?.primaryColor?.also { setIndicatorColor(it) }
}

internal fun ProgressBar.applyProgressColorTheme(colorTheme: ColorTheme?) {
    colorTheme?.primaryColorStateList?.also(::setIndeterminateTintList)
}

internal fun ImageView.applyImageColorTheme(colorTheme: ColorTheme?) {
    colorTheme?.primaryColorStateList?.also(::setImageTintList)
}

internal fun FloatingActionButton.applyBarButtonStatesTheme(barButtonStatesTheme: BarButtonStatesTheme?) {
    if (barButtonStatesTheme == null) return

    val colors: MutableList<Int> = mutableListOf()
    val states: MutableList<IntArray> = mutableListOf()

    val disabledState = intArrayOf(-android.R.attr.state_enabled)
    val activatedState = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_activated)
    val enabledState = intArrayOf()

    val oldBgTint = supportBackgroundTintList ?: backgroundTintList

    val bgDisabledColor = barButtonStatesTheme.disabled?.background?.primaryColor
        ?: oldBgTint.colorForState(disabledState)

    if (bgDisabledColor != null) {
        colors.add(bgDisabledColor)
        states.add(disabledState)
    }

    val bgActivatedColor = barButtonStatesTheme.activated?.background?.primaryColor
        ?: oldBgTint.colorForState(activatedState)

    if (bgActivatedColor != null) {
        colors.add(bgActivatedColor)
        states.add(activatedState)
    }

    val bgEnabledColor = barButtonStatesTheme.enabled?.background?.primaryColor
        ?: oldBgTint.colorForState(enabledState)

//        Default value must be at the latest position
    if (bgEnabledColor != null) {
        colors.add(bgEnabledColor)
        states.add(enabledState)
    }

    supportBackgroundTintList = ColorStateList(states.toTypedArray(), colors.toIntArray())
    states.clear()
    colors.clear()

    val oldImageTintList = supportImageTintList ?: imageTintList

    val disabledColor = barButtonStatesTheme.disabled?.imageColor?.primaryColor
        ?: oldImageTintList.colorForState(disabledState)

    if (disabledColor != null) {
        colors.add(disabledColor)
        states.add(disabledState)
    }

    val activatedColor = barButtonStatesTheme.activated?.imageColor?.primaryColor
        ?: oldImageTintList.colorForState(activatedState)

    if (activatedColor != null) {
        colors.add(activatedColor)
        states.add(activatedState)
    }

    val enabledColor = barButtonStatesTheme.enabled?.imageColor?.primaryColor
        ?: oldImageTintList.colorForState(enabledState)

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
            layerTheme,
            null,
            null,
            null
        )
    )
}
