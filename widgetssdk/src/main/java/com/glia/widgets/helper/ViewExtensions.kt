package com.glia.widgets.helper

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.os.Build
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
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.util.Consumer
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.core.view.children
import androidx.core.view.isVisible
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
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.textview.SingleLineHintEditText
import com.google.android.material.button.MaterialButton
import io.reactivex.rxjava3.disposables.Disposable

internal fun View.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(context, resId)
internal fun View.getColorStateListCompat(@ColorRes resId: Int) =
    ContextCompat.getColorStateList(context, resId)

internal fun View.getDrawableCompat(@DrawableRes resId: Int) =
    ContextCompat.getDrawable(context, resId)

internal fun View.getFontCompat(@FontRes resId: Int) = ResourcesCompat.getFont(context, resId)

@AnyRes
internal fun View.getAttr(@AttrRes attr: Int, @AnyRes fallBackResId: Int): Int =
    context.gliaAttrResourceId(attr, fallBackResId)

/**
 * Applies the composed theme's typeface to [views], resolving it once.
 *
 * The typeface is the one `glia*` value that cannot be a layout attribute, which is why every screen
 * needs this call rather than an `android:fontFamily` in XML. `android:fontFamily` on a view beats
 * its `textAppearance` unconditionally, so a layout could not express "use the theme's font, and
 * otherwise leave the `textAppearance`'s alone" - and [gliaAttrFont] is `null` unless the integrator
 * points `fontFamily` at a font *resource*, which is the common case.
 *
 * @param style an optional `Typeface` style to derive, for the few places whose `textAppearance`
 * weight has to survive the substitution. Omit it to assign the typeface as-is: `setTypeface(tf, 0)`
 * additionally clears fake-bold and skew, which is not the same thing.
 */
internal fun Context.applyGliaThemeFont(vararg views: TextView, style: Int? = null) {
    val font = gliaAttrFont() ?: return
    views.forEach { if (style == null) it.typeface = font else it.setTypeface(font, style) }
}

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
    registerLocaleListener(LocaleTarget.TEXT, stringKey, *values) { upToDateTranslation ->
        text = upToDateTranslation
    }
}

internal fun TextView.setText(locale: LocaleString?, listener: ((String) -> Unit)? = null) {
    if (locale == null) {
        clearLocaleListener(LocaleTarget.TEXT)
        text = ""
        return
    }
    registerLocaleListener(LocaleTarget.TEXT, locale.stringKey, *locale.values.toTypedArray()) { upToDateTranslation ->
        text = upToDateTranslation
        listener?.invoke(upToDateTranslation)
    }
}

internal fun TextView.setLocaleHint(@StringRes stringKey: Int, vararg values: StringKeyPair) {
    registerLocaleListener(LocaleTarget.HINT, stringKey, *values) { upToDateTranslation ->
        if (this is SingleLineHintEditText) {
            setHintOverride(upToDateTranslation)
        } else {
            hint = upToDateTranslation
        }
    }
}

internal fun TextView.setHint(locale: LocaleString?) {
    if (locale == null) {
        clearLocaleListener(LocaleTarget.HINT)
        hint = ""
        return
    }
    registerLocaleListener(LocaleTarget.HINT, locale.stringKey, *locale.values.toTypedArray()) { upToDateTranslation ->
        hint = upToDateTranslation
    }
}

/**
 * Sets an accessibility hint on the view.
 * The hint is announced before content description.
 */
internal fun View.setAccessibilityHint(hint: String) {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.hintText = hint
        }
    })
}

internal fun View.setAccessibilityHint(@StringRes stringKey: Int, vararg values: StringKeyPair) {
    registerLocaleListener(LocaleTarget.ACCESSIBILITY_HINT, stringKey, *values) { upToDateTranslation ->
        setAccessibilityHint(upToDateTranslation)
    }
}

internal fun View.setLocaleAccessibilityHint(locale: LocaleString) {
    registerLocaleListener(LocaleTarget.ACCESSIBILITY_HINT, locale.stringKey, *locale.values.toTypedArray()) { upToDateTranslation ->
        ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.hintText = upToDateTranslation
            }
        })
    }
}

internal fun View.setLocaleContentDescription(@StringRes stringKey: Int, vararg values: StringKeyPair) {
    registerLocaleListener(LocaleTarget.CONTENT_DESCRIPTION, stringKey, *values) { upToDateTranslation ->
        contentDescription = upToDateTranslation
    }
}

internal fun View.setPrefixedContentDescription(prefix: String, @StringRes stringKey: Int, vararg values: StringKeyPair) {
    registerLocaleListener(LocaleTarget.CONTENT_DESCRIPTION, stringKey, *values) { upToDateTranslation ->
        contentDescription = "$prefix. $upToDateTranslation"
    }
}

internal fun View.setContentDescription(locale: LocaleString?) {
    if (locale == null) {
        clearLocaleListener(LocaleTarget.CONTENT_DESCRIPTION)
        contentDescription = ""
        return
    }
    registerLocaleListener(LocaleTarget.CONTENT_DESCRIPTION, locale.stringKey, *locale.values.toTypedArray()) { upToDateTranslation ->
        contentDescription = upToDateTranslation
    }
}

internal fun Toolbar.setLocaleNavigationContentDescription(@StringRes stringKey: Int, vararg values: StringKeyPair) {
    registerLocaleListener(LocaleTarget.NAVIGATION_CONTENT_DESCRIPTION, stringKey, *values) { upToDateTranslation ->
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

/**
 * A view property that shows a localized string. Each one keeps its own locale listener in a view tag,
 * so setting the same property again replaces the old listener instead of adding another one.
 */
private enum class LocaleTarget(@param:IdRes val tagKey: Int) {
    TEXT(R.id.glia_locale_text),
    HINT(R.id.glia_locale_hint),
    CONTENT_DESCRIPTION(R.id.glia_locale_content_description),
    ACCESSIBILITY_HINT(R.id.glia_locale_accessibility_hint),
    NAVIGATION_CONTENT_DESCRIPTION(R.id.glia_locale_navigation_content_description)
}

/**
 * Listens for locale changes only while the view is attached.
 * The locale stream lives for the whole app, so a view that never attaches must not subscribe to it,
 * or it would keep its Activity in memory forever.
 */
private class LocaleListener(
    private val localeProvider: LocaleProvider,
    @param:StringRes private val stringKey: Int,
    private val values: List<StringKeyPair>,
    private val onTranslation: (String) -> Unit
) : View.OnAttachStateChangeListener {
    private var translation: String? = null
    private var disposable: Disposable? = null

    fun update() {
        val upToDateTranslation = localeProvider.getString(stringKey, values)
        if (upToDateTranslation == translation) return
        translation = upToDateTranslation
        onTranslation(upToDateTranslation)
    }

    override fun onViewAttachedToWindow(v: View) {
        // The locale may have changed while the view was detached.
        update()
        disposable = localeProvider.getLocaleObservable().subscribe({ update() }, { /* no-op */ })
    }

    override fun onViewDetachedFromWindow(v: View) {
        disposable?.dispose()
        disposable = null
    }
}

private fun View.registerLocaleListener(target: LocaleTarget, @StringRes stringKey: Int, vararg values: StringKeyPair, listener: (String) -> Unit) {
    val localeProvider = if (isInEditMode) LocaleProvider(ResourceProvider(context)) else Dependencies.localeProvider
    val localeListener = LocaleListener(localeProvider, stringKey, values.toList(), listener)

    clearLocaleListener(target)
    setTag(target.tagKey, localeListener)
    addOnAttachStateChangeListener(localeListener)
    if (isAttachedToWindow) localeListener.onViewAttachedToWindow(this) else localeListener.update()
}

private fun View.clearLocaleListener(target: LocaleTarget) {
    val localeListener = getTag(target.tagKey) as? LocaleListener ?: return
    removeOnAttachStateChangeListener(localeListener)
    localeListener.onViewDetachedFromWindow(this)
    setTag(target.tagKey, null)
}

/**
 * Ensures the view's visibility is set to the desired state.
 * The [then] action is only executed if the visibility actually changes,
 * and it receives the newly applied visibility state.
 */
internal fun View.ensureVisibility(isVisible: Boolean, then: (appliedState: Boolean) -> Unit = {}) {
    // If the state is already correct, do nothing.
    if (this.isVisible == isVisible) return

    // Apply the new state.
    this.isVisible = isVisible

    // Execute the callback, passing the new state.
    then(isVisible)
}
