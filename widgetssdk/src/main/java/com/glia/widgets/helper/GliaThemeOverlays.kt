@file:JvmName("GliaThemeOverlays")

package com.glia.widgets.helper

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.TypedValue
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.use
import com.glia.widgets.R

private const val TAG = "GliaThemeOverlays"
private const val NO_RESOURCE = 0

/**
 * Resources are static for the lifetime of the process, so both legacy-mechanism diagnostics are
 * worth reporting exactly once instead of on every Glia surface that is created.
 */
private var legacyOverlayDetectionLogged = false
private var unPrefixedLegacyItemsChecked = false

/**
 * Composes the Glia theme on top of the receiver's live `Resources.Theme`, in place.
 *
 * Precedence, lowest to highest:
 * 1. `Theme.Glia.Internal` - SDK defaults for every `glia*` attribute (already the receiver's theme).
 * 2. The legacy `gliaChatStyle` -> `materialThemeOverlay` overlay, if the integrator declares one.
 * 3. `GliaTheme` - the current customization hook, empty unless the integrator redefines it.
 *
 * The JSON Unified theme is applied later, in code, and keeps winning over all of the above.
 *
 * Both overlays are applied with `force = true`, so each one only replaces the items it actually
 * declares - `ThemeOverlay.Glia.Chat` (the parent of every legacy overlay) ships empty for exactly
 * that reason.
 *
 * @param includeLegacyOverlay `false` for surfaces that never went through the legacy
 * `MaterialThemeOverlay` chain - applying it there would be a new visual change for integrators
 * still on the legacy mechanism. See `ImagePreviewActivity`.
 * @param legacyActivityStyle only overridden by tests, see [resolveLegacyThemeOverlay].
 * @param customizationStyle only overridden by tests, which cannot redefine `GliaTheme` itself
 * without also re-theming the demo app.
 */
internal fun Context.applyGliaThemeOverlays(
    includeLegacyOverlay: Boolean = true,
    @StyleRes legacyActivityStyle: Int = R.style.Application_Glia_Activity_Style,
    @StyleRes customizationStyle: Int = R.style.GliaTheme
) {
    if (includeLegacyOverlay) {
        resolveLegacyThemeOverlay(legacyActivityStyle)?.let { theme.applyStyle(it, true) }
    }
    theme.applyStyle(customizationStyle, true)
}

/**
 * Returns a context whose theme is `Theme.Glia.Internal` plus the Glia overlays.
 *
 * Only needed where the SDK inflates into a context it does not own - an integrator's activity, a
 * `Service`, or the raw application context. Such a theme cannot be mutated in place: it is shared
 * with the host app and carries none of the `glia*` defaults.
 *
 * Views hosted by Glia activities need no wrapper - those activities compose their own theme.
 *
 * Must NOT be called from `attachBaseContext`. This resolves the theme eagerly, and at that point a
 * `Service`/`Activity` has not yet assigned its `mBase`, so reading the base theme walks
 * `ContextImpl.getTheme()` -> `getOuterContext()` -> `getApplicationInfo()` and throws
 * `NullPointerException`. Wrap with `ContextThemeWrapper(base, R.style.Theme_Glia_Internal)` there and
 * call [applyGliaThemeOverlays] once the component is attached - see `ChatHeadService`.
 */
internal fun Context.wrapWithGliaTheme(): Context =
    ContextThemeWrapper(this, R.style.Theme_Glia_Internal).apply { applyGliaThemeOverlays() }

/**
 * Resolves the `materialThemeOverlay` of the legacy `gliaChatStyle` style, or `null` when the
 * integrator does not use the legacy mechanism.
 *
 * @param legacyActivityStyle the resource integrators override to carry `gliaChatStyle`. Only
 * overridden by tests, which must not redefine the library resource itself.
 */
@StyleRes
internal fun Context.resolveLegacyThemeOverlay(
    @StyleRes legacyActivityStyle: Int = R.style.Application_Glia_Activity_Style
): Int? {
    val legacyChatStyle = resolveLegacyChatStyle(legacyActivityStyle) ?: return null

    logLegacyMechanismUsage(legacyChatStyle)

    return theme
        .gliaResourceIds(legacyChatStyle, intArrayOf(com.google.android.material.R.attr.materialThemeOverlay))
        .first()
        .takeIf { it != NO_RESOURCE }
}

/**
 * Looks for `gliaChatStyle` in two places, in order:
 * 1. The live theme - covers `gliaChatStyle` declared anywhere in the current theme hierarchy,
 *    including the host application theme.
 * 2. A scratch theme built from `Application.Glia.Activity.Style` - covers the documented legacy
 *    pattern where the integrator overrides that resource to carry `gliaChatStyle`.
 */
@StyleRes
private fun Context.resolveLegacyChatStyle(@StyleRes legacyActivityStyle: Int): Int? =
    theme.resolveGliaChatStyle() ?: scratchTheme(legacyActivityStyle).resolveGliaChatStyle()

private fun Context.scratchTheme(@StyleRes styleRes: Int): Resources.Theme =
    resources.newTheme().apply { applyStyle(styleRes, true) }

@StyleRes
private fun Resources.Theme.resolveGliaChatStyle(): Int? {
    val typedValue = TypedValue()
    if (!resolveAttribute(R.attr.gliaChatStyle, typedValue, true)) return null
    return typedValue.resourceId.takeIf { it != NO_RESOURCE }
}

/**
 * Whether the given legacy style sets any un-prefixed `R.styleable.GliaView` item itself.
 *
 * Those twins (`brandPrimaryColor` and friends) were never a supported way to customize the SDK:
 * layouts have always resolved the `glia*`-prefixed theme attributes and ignored them. They are no
 * longer honoured anywhere, so integrators that still set them need pointing at the 1:1 prefixed
 * replacement.
 *
 * `Application.Glia.Chat` - the documented parent of every legacy `gliaChatStyle` style - declares
 * the whole styleable as `?attr/glia*` indirections, so "has a value" alone cannot tell an inherited
 * item from one the integrator set. Comparing against that baseline can.
 */
internal fun Context.declaresUnPrefixedLegacyItems(@StyleRes legacyChatStyle: Int): Boolean {
    val baseline = theme.gliaResourceIds(R.style.Application_Glia_Chat, R.styleable.GliaView)
    val actual = theme.gliaResourceIds(legacyChatStyle, R.styleable.GliaView)
    return !baseline.contentEquals(actual)
}

private fun Context.logLegacyMechanismUsage(@StyleRes legacyChatStyle: Int) {
    if (!legacyOverlayDetectionLogged) {
        legacyOverlayDetectionLogged = true
        Logger.i(TAG, "Legacy gliaChatStyle theming detected")
    }

    if (unPrefixedLegacyItemsChecked) return
    unPrefixedLegacyItemsChecked = true

    if (!declaresUnPrefixedLegacyItems(legacyChatStyle)) return

    Logger.w(
        TAG,
        "The style set as gliaChatStyle declares un-prefixed GliaView attributes (for example " +
            "brandPrimaryColor). Those are no longer applied. Use the glia-prefixed attribute of the " +
            "same name (gliaBrandPrimaryColor) inside your materialThemeOverlay, or migrate to the " +
            "GliaTheme style."
    )
}

private fun Resources.Theme.gliaResourceIds(@StyleRes styleRes: Int, attrs: IntArray): IntArray =
    obtainStyledAttributes(styleRes, attrs).use { it.resourceIds() }

private fun TypedArray.resourceIds(): IntArray = IntArray(length()) { getResourceId(it, NO_RESOURCE) }
