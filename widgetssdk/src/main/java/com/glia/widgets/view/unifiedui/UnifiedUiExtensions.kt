package com.glia.widgets.view.unifiedui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.StateSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.glia.widgets.R
import com.glia.widgets.helper.applyShadow
import com.glia.widgets.helper.colorForState
import com.glia.widgets.helper.setCompoundDrawableTintListCompat
import com.glia.widgets.view.button.GliaSurveyOptionButton
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.call.BarButtonStatesTheme
import com.glia.widgets.view.unifiedui.theme.call.BarButtonStyleTheme
import com.glia.widgets.view.unifiedui.theme.survey.OptionButtonTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.shape.CornerFamily

internal fun View.applyColorTheme(defaultColor: ColorTheme?, disabledColor: ColorTheme? = null) {
    createDrawableStateList(defaultColor, disabledColor)?.let {
        background = it
        backgroundTintList = null
    }
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
internal fun View.applyLayerTheme(defTheme: LayerTheme?, disabledTheme: LayerTheme? = null) {
    if (defTheme?.fill == null && defTheme?.stroke == null) return

    var drawable: GradientDrawable? = null
    // The part below is needed to preserve GradientDrawable/background properties that are `null`.
    // For example without this implementation if `cornerRadius = null` but `stroke = Color.RED` then
    // whatever corner radios is in the default UI implementation it would automatically become 0 instead
    if (background is StateListDrawable) {
        drawable = background.current as? GradientDrawable
    } else if (background is GradientDrawable) {
        drawable = background as GradientDrawable
    }
    if (drawable == null) {
        drawable = GradientDrawable()
    }

    if (backgroundTintList != null) {
        drawable.color = backgroundTintList
        backgroundTintList = null
    }

    applyLayerTheme(drawable, defTheme, disabledTheme)
}

private fun View.applyLayerTheme(originalDrawable: GradientDrawable?, defTheme: LayerTheme?, disabledTheme: LayerTheme?) {
    if (originalDrawable == null || defTheme == null) return
    val newBackground = StateListDrawable()
    newBackground.addState(
        StateSet.WILD_CARD,
        originalDrawable.mutateWithSettings(defTheme, context)
    )
    if (disabledTheme != null) {
        newBackground.addState(
            intArrayOf(-android.R.attr.state_enabled),
            originalDrawable.mutateWithSettings(disabledTheme, context)
        )
    }

    background = newBackground
}


private fun GradientDrawable.mutateWithSettings(theme: LayerTheme?, context: Context): GradientDrawable {
    return mutateWithSettings(theme, context.resources.getDimensionPixelSize(R.dimen.glia_px))
}

private fun GradientDrawable.mutateWithSettings(theme: LayerTheme?, fallbackStrokeWidth: Int): GradientDrawable {
    val clone = (mutate() as? GradientDrawable) ?: GradientDrawable()

    if (theme == null) return clone
    theme.fill?.let {
        if (it.isGradient) {
            clone.colors = it.valuesArray
        } else {
            clone.setColor(it.primaryColor)
        }
    }

    theme.stroke?.let {
        clone.setStroke(
            theme.borderWidthInt ?: fallbackStrokeWidth,
            it
        )
    }

    theme.cornerRadius?.let {
        clone.cornerRadius = it
    }

    return clone
}

internal fun ShapeableImageView.applyLayerTheme(layer: LayerTheme?) {
    layer?.fill?.also {
        val drawable = (background as? GradientDrawable) ?: GradientDrawable()
        if (it.isGradient) {
            drawable.colors = it.valuesArray
        } else {
            drawable.setColor(it.primaryColor)
        }
        background = drawable
    }

    layer?.stroke?.also { strokeColor = ColorStateList.valueOf(it) }
    layer?.borderWidth?.also(::setStrokeWidth)

    layer?.cornerRadius?.also {
        shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCorners(CornerFamily.ROUNDED, it).build()
    }
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
        paint.shader = null
        setTextColor(color.primaryColor)
    }

    // Line below apply text color to any drawable that are part of this TextVIew
    // e.g.: app:drawableStartCompat="@drawable/ic_attention"
    color.primaryColorStateList.let(::setCompoundDrawableTintList)
}

internal fun TextView.applyTextColor(defaultColor: Int, disabledColor: Int? = null) {
    setTextColor(createColorStateList(defaultColor, disabledColor))
    setCompoundDrawableTintListCompat(createColorStateList(defaultColor, disabledColor))
}

internal fun TextView.applyHintColor(defaultColor: Int, disabledColor: Int? = null) {
    setHintTextColor(createColorStateList(defaultColor, disabledColor))
}

internal fun TextView.applyHintTheme(defaultTheme: TextTheme?, disabledTheme: TextTheme? = null) {
    defaultTheme?.textColor?.primaryColor?.let {
        applyHintColor(it, disabledTheme?.textColor?.primaryColor)
    }
}

internal fun TextView.applyTextTheme(
    textTheme: TextTheme?,
    withBackground: Boolean = false,
    withAlignment: Boolean = true
) {
    textTheme?.textColor.also(::applyTextColorTheme)
    textTheme?.textSize?.also { setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
    textTheme?.textStyle?.let { typeface = Typeface.create(typeface, it) }
        ?: run {
            // Edge case fix where some TextView default styles are using narrow font
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                typeface = Typeface.SANS_SERIF
            }
    }

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
        bg.stroke?.also { borderColor ->
            strokeColor = ColorStateList.valueOf(borderColor)
            // If `borderWidth` is `null` even though `strokeColor` is provided then use default border width
            strokeWidth = bg.borderWidthInt ?: resources.getDimensionPixelSize(R.dimen.glia_px)
            bg.cornerRadiusInt?.also { cornerRadius = it }
        }
    }

    buttonTheme?.elevation?.also {
        stateListAnimator = null // Default state list controls the button shadow (elevation)
        elevation = it
    }
    buttonTheme?.shadowColor?.also(::applyShadow)
    buttonTheme?.text?.also {
        applyTextTheme(it)
    }

    // If no `iconColor` provided use text color
    buttonTheme?.let { it.iconColor ?: it.text?.textColor }?.primaryColorStateList?.also {
        iconTint = it
    }
}

internal fun ImageView.applyButtonTheme(buttonMainTheme: ButtonTheme?, buttonDisabledTheme: ButtonTheme? = null) {
    applyImageColorTheme(buttonMainTheme?.iconColor, buttonDisabledTheme?.iconColor)
}

internal fun CircularProgressIndicator.applyIndicatorColorTheme(colorTheme: ColorTheme?) {
    colorTheme?.primaryColor?.also { setIndicatorColor(it) }
}

internal fun ProgressBar.applyProgressColorTheme(colorTheme: ColorTheme?) {
    colorTheme?.primaryColorStateList?.also(::setIndeterminateTintList)
}

internal fun ImageView.applyImageColor(defaultColor: Int, disabledColor: Int? = null) {
    imageTintList = createColorStateList(defaultColor, disabledColor)
}

internal fun ImageView.applyImageColorTheme(defaultTheme: ColorTheme?, disabledTheme: ColorTheme? = null) {
    defaultTheme?.primaryColor?.let { applyImageColor(it, disabledTheme?.primaryColor) }
}

private fun createColorStateList(defaultColor: Int?, disabledColor: Int?): ColorStateList? {
    if (defaultColor == null) return null
    if (disabledColor == null) return ColorStateList.valueOf(defaultColor)

    // For future -> default state list can be found here:
    // https://developer.android.com/guide/topics/resources/color-list-resource
    return ColorStateList(
        arrayOf( // Reminder: order maters
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(), // empty array -> default fallback
        ),
        intArrayOf(
            disabledColor,
            defaultColor
        )
    )
}

private fun createDrawableStateList(defColor: ColorTheme?, disabledColor: ColorTheme? = null): StateListDrawable? {
    if (defColor == null) return null
    val stateList = StateListDrawable()
    stateList.addState(StateSet.WILD_CARD, createBackgroundFromTheme(defColor))

    if (disabledColor == null) return stateList
    stateList.addState(intArrayOf(-android.R.attr.state_enabled), createBackgroundFromTheme(disabledColor))
    return stateList
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

internal fun ImageButton.applyBarButtonStatesTheme(barButtonStatesTheme: BarButtonStatesTheme?) {
    if (barButtonStatesTheme == null) return

    applyBarButtonStyleTheme(
        barButtonStatesTheme.enabled,
        barButtonStatesTheme.disabled,
        barButtonStatesTheme.activated
    )
}

internal fun ImageButton.applyBarButtonStyleTheme(barButtonStyleTheme: BarButtonStyleTheme?) {
    applyBarButtonStyleTheme(barButtonStyleTheme, null, null)
}

private fun ImageButton.applyBarButtonStyleTheme(
    enabled: BarButtonStyleTheme?,
    disabled: BarButtonStyleTheme?,
    activated: BarButtonStyleTheme?
) {
    if (enabled == null && disabled == null && activated == null) return

    val colors: MutableList<Int> = mutableListOf()
    val states: MutableList<IntArray> = mutableListOf()

    val disabledState = intArrayOf(-android.R.attr.state_enabled)
    val activatedState = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_activated)
    val enabledState = intArrayOf()

    val oldBgTint = backgroundTintList

    val bgDisabledColor = disabled?.background?.primaryColor
        ?: oldBgTint.colorForState(disabledState)

    if (bgDisabledColor != null) {
        colors.add(bgDisabledColor)
        states.add(disabledState)
    }

    val bgActivatedColor = activated?.background?.primaryColor
        ?: oldBgTint.colorForState(activatedState)

    if (bgActivatedColor != null) {
        colors.add(bgActivatedColor)
        states.add(activatedState)
    }

    val bgEnabledColor = enabled?.background?.primaryColor
        ?: oldBgTint.colorForState(enabledState)

//        Default value must be at the latest position
    if (bgEnabledColor != null) {
        colors.add(bgEnabledColor)
        states.add(enabledState)
    }

    backgroundTintList = ColorStateList(states.toTypedArray(), colors.toIntArray())
    states.clear()
    colors.clear()

    val oldImageTintList = imageTintList

    val disabledColor = disabled?.imageColor?.primaryColor
        ?: oldImageTintList.colorForState(disabledState)

    if (disabledColor != null) {
        colors.add(disabledColor)
        states.add(disabledState)
    }

    val activatedColor = activated?.imageColor?.primaryColor
        ?: oldImageTintList.colorForState(activatedState)

    if (activatedColor != null) {
        colors.add(activatedColor)
        states.add(activatedState)
    }

    val enabledColor = enabled?.imageColor?.primaryColor
        ?: oldImageTintList.colorForState(enabledState)

    if (enabledColor != null) {
        colors.add(enabledColor)
        states.add(enabledState)
    }

    imageTintList = ColorStateList(states.toTypedArray(), colors.toIntArray())
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
