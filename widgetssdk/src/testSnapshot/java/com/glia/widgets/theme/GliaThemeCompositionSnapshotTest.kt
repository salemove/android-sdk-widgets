package com.glia.widgets.theme

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.helper.applyGliaThemeOverlays
import com.glia.widgets.snapshotutils.SnapshotDialog
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogType
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

/**
 * The end-to-end proof of the theming mechanism: instead of rendering against a hand-written theme,
 * every case here builds its context the way production does - `Theme.Glia.Internal` plus the real
 * [applyGliaThemeOverlays] chokepoint - and snapshots what comes out.
 *
 * The subject is the option dialog because it reads the palette twice over: the title and message
 * resolve `?attr/gliaBaseDarkColor` through the layout, while the two buttons are painted in code
 * from `alertDialogConfiguration`, which merges the same theme attributes with the JSON theme. One
 * image therefore covers both halves of the pipeline and its precedence rule.
 *
 * Reading the goldens:
 *
 * | Layer               | `gliaBrandPrimaryColor` | `gliaSystemNegativeColor` | `gliaBaseDarkColor` |
 * |---------------------|-------------------------|---------------------------|---------------------|
 * | SDK default         | Glia purple             | Glia red                  | Glia dark           |
 * | legacy overlay      | teal                    | pink                      | -                   |
 * | `GliaTheme`         | orange                  | -                         | indigo              |
 *
 * so in [legacyAndCustomization] the positive button must be orange (customization wins the
 * overlap), the negative button pink (legacy keeps what customization does not declare) and the text
 * indigo.
 */
internal class GliaThemeCompositionSnapshotTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotDialog {

    private val dialogType = DialogType.Option(
        DialogPayload.Option(
            title = title,
            message = message,
            positiveButtonText = positiveButtonText,
            negativeButtonText = negativeButtonText,
            poweredByText = poweredByText,
            positiveButtonClickListener = {},
            negativeButtonClickListener = {}
        )
    )

    /**
     * Mirrors production: a context themed with `Theme.Glia.Internal`, composed in place.
     *
     * The two style parameters are [applyGliaThemeOverlays]'s test seams. They default to the real
     * `Application.Glia.Activity.Style` and `GliaTheme` resources, both of which ship blank - so the
     * defaults are also the "integrator customizes nothing" case.
     */
    private fun composedContext(
        @StyleRes legacyActivityStyle: Int = R.style.Application_Glia_Activity_Style,
        @StyleRes customizationStyle: Int = R.style.GliaTheme
    ): Context = ContextThemeWrapper(context, R.style.Theme_Glia_Internal).apply {
        applyGliaThemeOverlays(
            legacyActivityStyle = legacyActivityStyle,
            customizationStyle = customizationStyle
        )
    }

    private fun snapshotComposition(
        @StyleRes legacyActivityStyle: Int = R.style.Application_Glia_Activity_Style,
        @StyleRes customizationStyle: Int = R.style.GliaTheme,
        unifiedTheme: UnifiedTheme? = null
    ) {
        val composedContext = composedContext(legacyActivityStyle, customizationStyle)
        snapshot(inflateView(context = composedContext, unifiedTheme = unifiedTheme, dialogType = dialogType))
    }

    /** Neither mechanism in use: the composition must be indistinguishable from the SDK defaults. */
    @Test
    fun default() {
        snapshotComposition()
    }

    /** Legacy `gliaChatStyle` -> `materialThemeOverlay` only: its two items replace the defaults. */
    @Test
    fun legacyOverlayOnly() {
        snapshotComposition(legacyActivityStyle = R.style.Test_Glia_Composition_ActivityStyle)
    }

    /** New mechanism only: `GliaTheme`'s two items replace the defaults, nothing else moves. */
    @Test
    fun customizationOnly() {
        snapshotComposition(customizationStyle = R.style.Test_Glia_Composition_Customization)
    }

    /**
     * Both mechanisms at once - the precedence case. `GliaTheme` wins the attribute both declare and
     * the legacy overlay keeps the one only it declares.
     */
    @Test
    fun legacyAndCustomization() {
        snapshotComposition(
            legacyActivityStyle = R.style.Test_Glia_Composition_ActivityStyle,
            customizationStyle = R.style.Test_Glia_Composition_Customization
        )
    }

    /** The JSON Unified theme is applied last in code, so it still wins over the XML mechanism. */
    @Test
    fun customizationAndUnifiedTheme() {
        snapshotComposition(
            customizationStyle = R.style.Test_Glia_Composition_Customization,
            unifiedTheme = unifiedTheme()
        )
    }
}
