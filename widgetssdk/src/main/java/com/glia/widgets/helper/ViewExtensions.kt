package com.glia.widgets.helper

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.AnyRes
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StyleableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.widget.TextViewCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

internal fun View.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(context, resId)
internal fun View.getColorStateListCompat(@ColorRes resId: Int) =
    ContextCompat.getColorStateList(context, resId)

internal fun View.getDrawableCompat(@DrawableRes resId: Int) =
    ContextCompat.getDrawable(context, resId)

internal fun View.getFontCompat(@FontRes resId: Int) = ResourcesCompat.getFont(context, resId)

@AnyRes
internal fun View.getTypedArrayResId(
    typedArray: TypedArray, @StyleableRes index: Int, @AttrRes fallbackAttr: Int
) = Utils.getTypedArrayIntegerValue(typedArray, context, index, fallbackAttr)

@AnyRes
internal fun View.getAttr(@AttrRes attr: Int, @AnyRes fallBackResId: Int): Int =
    TypedValue().takeIf {
        context.theme.resolveAttribute(attr, it, true)
    }?.resourceId ?: fallBackResId

internal fun ViewGroup.hasChildOfType(clazz: Class<*>) = children.any { clazz.isInstance(it) }
internal fun MenuItem.applyIconColorTheme(@ColorInt iconColor: Int?) {
    iconColor?.let { icon?.setTintCompat(it) }
}

internal fun ImageView.applyImageColorTheme(@ColorInt imageColor: Int?) {
    imageColor?.let { imageTintList = ColorStateList.valueOf(it) }
}

internal fun ProgressBar.applyProgressColorTheme(@ColorInt progressColor: Int?) {
    progressColor?.also { indeterminateTintList = ColorStateList.valueOf(it) }
}

internal fun View.changeStatusBarColor(color: Int?) {
    color?.run { context.asActivity()?.window?.statusBarColor = this }
}

internal fun View.applyShadow(@ColorInt color: Int?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && color != null) {
        outlineSpotShadowColor = color
        outlineAmbientShadowColor = color
    }
}

internal fun MaterialToolbar.applyToolbarTheme(
    @ColorInt backgroundColor: Int?,
    @ColorInt navigationIconColor: Int?
) {
    backgroundColor?.let {
        backgroundTintList = ColorStateList.valueOf(it)
    }
    navigationIconColor?.let {
        navigationIcon?.setTint(navigationIconColor)
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

internal fun TextView.applyTextTheme(
    @ColorInt textColor: Int?,
    textFont: Typeface?
) {
    textColor?.also(::setTextColor)
    textFont?.also(::setTypeface)
}

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
internal fun TextView.setCompoundDrawableTintListCompat(tint: ColorStateList?) {
    TextViewCompat.setCompoundDrawableTintList(this, tint)
}