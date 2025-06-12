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
import androidx.annotation.StringRes
import androidx.annotation.StyleableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.util.Consumer
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.core.view.children
import androidx.core.widget.TextViewCompat
import coil3.SingletonImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.request.target
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.glia.widgets.di.Dependencies
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.textview.SingleLineHintEditText
import com.google.android.material.button.MaterialButton

internal fun View.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(context, resId)
internal fun View.getColorStateListCompat(@ColorRes resId: Int) =
    ContextCompat.getColorStateList(context, resId)

internal fun View.getDrawableCompat(@DrawableRes resId: Int) =
    ContextCompat.getDrawable(context, resId)

internal fun View.getFontCompat(@FontRes resId: Int) = ResourcesCompat.getFont(context, resId)

@AnyRes
internal fun View.getTypedArrayResId(
    typedArray: TypedArray,
    @StyleableRes index: Int,
    @AttrRes fallbackAttr: Int
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

internal fun View.applyShadow(@ColorInt color: Int?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && color != null) {
        outlineSpotShadowColor = color
        outlineAmbientShadowColor = color
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
    onSuccess: Consumer<Unit>? = null,
    onError: Consumer<Throwable>? = null
) {

    val imageListener = object : ImageRequest.Listener {
        override fun onSuccess(request: ImageRequest, result: SuccessResult) {
            onSuccess?.accept(Unit)
        }

        override fun onError(request: ImageRequest, result: ErrorResult) {
            onError?.accept(result.throwable)
        }
    }

    val request = ImageRequest.Builder(context)
        .data(url)
        .crossfade(true) // A crossfade animation when a request completes successfully.
        .listener(imageListener)
        // Disable hardware bitmaps to avoid 'java.lang.IllegalArgumentException: Software rendering doesn't support hardware bitmaps' exception.
        // See https://coil-kt.github.io/coil/recipes/#shared-element-transitions
        .allowHardware(false)
        .target(this)
        .build()

    SingletonImageLoader.get(context).enqueue(request)
}

internal val View.layoutInflater: LayoutInflater get() = LayoutInflater.from(this.context)
internal fun TextView.setCompoundDrawableTintListCompat(tint: ColorStateList?) {
    TextViewCompat.setCompoundDrawableTintList(this, tint)
}

internal fun TextView.setLocaleText(@StringRes stringKey: Int, vararg values: StringKeyPair) {
    registerLocaleListener(stringKey, *values) { upToDateTranslation ->
        text = upToDateTranslation
    }
}

internal fun TextView.setText(locale: LocaleString?, listener: ((String) -> Unit)? = null) {
    if (locale == null) {
        text = ""
        return
    }
    registerLocaleListener(locale.stringKey, *locale.values.toTypedArray()) { upToDateTranslation ->
        text = upToDateTranslation
        listener?.invoke(upToDateTranslation)
    }
}

internal fun TextView.setLocaleHint(@StringRes stringKey: Int, vararg values: StringKeyPair) {
    registerLocaleListener(stringKey, *values) { upToDateTranslation ->
        if (this is SingleLineHintEditText) {
            setHintOverride(upToDateTranslation)
        } else {
            hint = upToDateTranslation
        }
    }
}

internal fun TextView.setHint(locale: LocaleString?) {
    if (locale == null) {
        hint = ""
        return
    }
    registerLocaleListener(locale.stringKey, *locale.values.toTypedArray()) { upToDateTranslation ->
        hint = upToDateTranslation
    }
}

internal fun View.setLocaleContentDescription(@StringRes stringKey: Int, vararg values: StringKeyPair) {
    registerLocaleListener(stringKey, *values) { upToDateTranslation ->
        contentDescription = upToDateTranslation
    }
}

internal fun View.setContentDescription(locale: LocaleString?) {
    if (locale == null) {
        contentDescription = ""
        return
    }
    registerLocaleListener(locale.stringKey, *locale.values.toTypedArray()) { upToDateTranslation ->
        contentDescription = upToDateTranslation
    }
}

internal fun Toolbar.setLocaleNavigationContentDescription(@StringRes stringKey: Int, vararg values: StringKeyPair) {
    registerLocaleListener(stringKey, *values) { upToDateTranslation ->
        navigationContentDescription = upToDateTranslation
    }
}

internal fun View.addClickActionAccessibilityLabel(label: String) {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.addAction(AccessibilityActionCompat(AccessibilityNodeInfoCompat.ACTION_CLICK, label))
        }
    })
}

internal fun View.removeAccessibilityClickAction() {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.removeAction(AccessibilityActionCompat.ACTION_CLICK)
            info.isClickable = false
        }
    })
}

private fun View.registerLocaleListener(@StringRes stringKey: Int, vararg values: StringKeyPair, listener: (String) -> Unit) {
    val localeManager = if (isInEditMode) LocaleProvider(ResourceProvider(context)) else Dependencies.localeProvider
    val disposable = localeManager.getLocaleObservable()
        .startWithItem("stub")
        .map { localeManager.getString(stringKey, values.toList()) }
        .distinctUntilChanged()
        .subscribe(
            { listener(it) },
            { /* no-op */ }
        )

    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            /* no-op */
        }

        override fun onViewDetachedFromWindow(v: View) {
            disposable.dispose()
            removeOnAttachStateChangeListener(this)
        }
    })
}
