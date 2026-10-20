package com.glia.widgets.view.dialog.base

import android.content.Context
import android.graphics.Color
import android.view.ContextThemeWrapper
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.glia.widgets.R
import com.glia.widgets.helper.applyGliaThemeOverlays
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Locks the dialog styling to the composed theme: every value the deleted `UiTheme.alertTheme`
 * projection used to carry now comes from a `glia*` attribute, and the JSON Unified theme still wins
 * over all of it.
 *
 * Fixtures are the ones described in `src/debug/res/values/glia_theme_overlays_test_themes.xml`.
 */
@RunWith(RobolectricTestRunner::class)
internal class AlertDialogConfigurationFactoryTest {

    private val application: Context get() = RuntimeEnvironment.getApplication()

    private fun themed(@StyleRes themeRes: Int = R.style.Theme_Glia_Internal): Context = ContextThemeWrapper(application, themeRes)

    private fun Context.color(colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

    @Test
    fun `color pallet is built from the glia colour attributes`() {
        val context = themed()

        val pallet = requireNotNull(context.alertDialogConfiguration(null).theme.alertTheme)

        assertEquals(ColorTheme(context.color(R.color.glia_primary_color)), pallet.titleImageColor)
        assertEquals(ColorTheme(context.color(R.color.glia_light_color)), pallet.backgroundColor)
        assertEquals(ColorTheme(context.color(R.color.glia_normal_color)), pallet.closeButtonColor)
    }

    @Test
    fun `customizing a colour attribute reaches the dialog`() {
        val context = themed().apply { applyGliaThemeOverlays(customizationStyle = R.style.Test_Glia_Customization) }

        val alertTheme = requireNotNull(context.alertDialogConfiguration(null).theme.alertTheme)

        assertEquals(ColorTheme(context.color(R.color.glia_test_customization_brand)), alertTheme.titleImageColor)
    }

    @Test
    fun `close icon comes from gliaIconLeaveQueue`() {
        val default = themed()
        val customized = themed().apply { applyGliaThemeOverlays(customizationStyle = R.style.Test_Glia_Customization) }

        assertEquals(R.drawable.ic_baseline_close, default.alertDialogConfiguration(null).icons.iconLeaveQueue)
        assertEquals(R.drawable.glia_test_customization_icon, customized.alertDialogConfiguration(null).icons.iconLeaveQueue)
    }

    @Test
    fun `both booleans default to false and follow their attributes`() {
        val default = themed().alertDialogConfiguration(null)

        assertFalse(default.theme.alertTheme?.isVerticalAxis == true)
        assertFalse(default.theme.isWhiteLabel == true)

        val flipped = themed()
            .apply { applyGliaThemeOverlays(customizationStyle = R.style.Test_Glia_Customization_Booleans) }
            .alertDialogConfiguration(null)

        assertTrue(flipped.theme.alertTheme?.isVerticalAxis == true)
        assertTrue(flipped.theme.isWhiteLabel == true)
    }

    @Test
    fun `unified theme wins over the theme attributes`() {
        val context = themed().apply { applyGliaThemeOverlays(customizationStyle = R.style.Test_Glia_Customization) }
        val unifiedTheme = UnifiedTheme(
            alertTheme = AlertTheme(backgroundColor = ColorTheme(Color.RED), isVerticalAxis = true),
            isWhiteLabel = true
        )

        val configuration = context.alertDialogConfiguration(unifiedTheme)

        assertEquals(ColorTheme(Color.RED), configuration.theme.alertTheme?.backgroundColor)
        assertTrue(configuration.theme.alertTheme?.isVerticalAxis == true)
        assertTrue(configuration.theme.isWhiteLabel == true)
    }

    @Test
    fun `unified theme only replaces what it declares`() {
        val context = themed()
        val unifiedTheme = UnifiedTheme(alertTheme = AlertTheme(backgroundColor = ColorTheme(Color.RED)))

        val alertTheme = requireNotNull(context.alertDialogConfiguration(unifiedTheme).theme.alertTheme)

        assertEquals(ColorTheme(Color.RED), alertTheme.backgroundColor)
        assertEquals(ColorTheme(context.color(R.color.glia_primary_color)), alertTheme.titleImageColor)
    }

    @Test
    fun `a legacy overlay reaches the dialog too`() {
        val context = themed(R.style.Test_Glia_Theme_WithLegacyChatStyle).apply { applyGliaThemeOverlays() }

        val alertTheme = requireNotNull(context.alertDialogConfiguration(null).theme.alertTheme)

        assertEquals(ColorTheme(context.color(R.color.glia_test_legacy_brand)), alertTheme.titleImageColor)
    }

    @Test
    fun `a host theme with no glia attributes falls back to the SDK colour resources`() {
        val context = themed(R.style.Test_Glia_Theme_HostApp)

        val alertTheme = requireNotNull(context.alertDialogConfiguration(null).theme.alertTheme)

        assertEquals(ColorTheme(context.color(R.color.glia_light_color)), alertTheme.backgroundColor)
        assertNull(context.alertDialogConfiguration(null).icons.iconLeaveQueue)
    }

    @Test
    fun `no typeface is produced while fontFamily names a family instead of a font resource`() {
        assertNull(themed().alertDialogConfiguration(null).properties.typeface)
    }
}
