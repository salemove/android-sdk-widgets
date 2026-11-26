package com.glia.widgets.helper

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.glia.widgets.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Locks the theme-composition precedence contract:
 * `Theme.Glia.Internal` < legacy `gliaChatStyle` overlay < `GliaTheme`.
 *
 * Fixtures live in `src/debug/res/values/glia_theme_overlays_test_*.xml`; see the comment there for
 * why they are not in the test source set and why `Test.Glia.Customization` stands in for a
 * redefined `GliaTheme`.
 */
@RunWith(RobolectricTestRunner::class)
internal class GliaThemeOverlaysTest {

    private val application: Context get() = RuntimeEnvironment.getApplication()

    private fun themed(@StyleRes themeRes: Int): Context = ContextThemeWrapper(application, themeRes)

    private fun Context.resourceIdOf(@AttrRes attr: Int): Int = TypedValue()
        .let { if (theme.resolveAttribute(attr, it, true)) it.resourceId else 0 }

    private fun Context.booleanOf(@AttrRes attr: Int): Boolean = TypedValue()
        .let { theme.resolveAttribute(attr, it, true) && it.data != 0 }

    // region defaults

    @Test
    fun `Theme Glia Internal supplies the SDK defaults for glia attributes`() {
        val context = themed(R.style.Theme_Glia_Internal)

        assertEquals(R.color.glia_primary_color, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.color.glia_dark_color, context.resourceIdOf(R.attr.gliaBaseDarkColor))
        assertEquals(R.color.glia_light_color, context.resourceIdOf(R.attr.gliaBaseLightColor))
        assertEquals(R.drawable.ic_baseline_send, context.resourceIdOf(R.attr.gliaIconSendMessage))
        assertEquals(R.drawable.ic_baseline_arrow_back, context.resourceIdOf(R.attr.gliaIconAppBarBack))
        assertEquals(R.drawable.ic_baseline_close, context.resourceIdOf(R.attr.gliaIconLeaveQueue))
        assertFalse(context.booleanOf(R.attr.whiteLabel))
        assertFalse(context.booleanOf(R.attr.gliaAlertDialogButtonUseVerticalAlignment))
    }

    @Test
    fun `Theme Glia Internal supplies the non-glia infrastructure attributes`() {
        val context = themed(R.style.Theme_Glia_Internal)

        assertEquals(
            R.style.ThemeOverlay_Glia_Chat_AlertDialog,
            context.resourceIdOf(com.google.android.material.R.attr.materialAlertDialogTheme)
        )
        assertEquals(R.style.Application_Glia_Chat_ChatHead, context.resourceIdOf(R.attr.chatHeadStyle))
        assertEquals(R.style.Application_Glia_ChoiceCard_ContentText, context.resourceIdOf(R.attr.choiceCardContentTextStyle))
        assertEquals(R.style.Application_Glia_Chat_Button_Positive, context.resourceIdOf(R.attr.buttonBarPositiveButtonStyle))
        assertEquals(R.style.Application_Glia_Chat_Button_Gva, context.resourceIdOf(R.attr.gvaOptionButtonStyle))
        assertEquals(R.style.Application_Glia_Header_EndButton, context.resourceIdOf(R.attr.gliaHeaderEndButtonStyle))
    }

    @Test
    fun `GliaTheme ships empty so partial redefinition is safe`() {
        val context = themed(R.style.Theme_Glia_Internal)

        context.theme.applyStyle(R.style.GliaTheme, true)

        assertEquals(R.color.glia_primary_color, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.drawable.ic_baseline_send, context.resourceIdOf(R.attr.gliaIconSendMessage))
    }

    @Test
    fun `ThemeOverlay Glia Chat is empty so that legacy overlays only contribute their own items`() {
        val context = themed(R.style.Theme_Glia_Internal)

        context.theme.applyStyle(R.style.ThemeOverlay_Glia_Chat, true)

        assertEquals(R.color.glia_primary_color, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.drawable.ic_baseline_send, context.resourceIdOf(R.attr.gliaIconSendMessage))
    }

    // endregion

    // region legacy resolution

    @Test
    fun `resolveLegacyThemeOverlay returns null when no gliaChatStyle is declared anywhere`() {
        val overlay = themed(R.style.Theme_Glia_Internal)
            .resolveLegacyThemeOverlay(R.style.Test_Glia_Legacy_ActivityStyleWithoutChatStyle)

        assertNull(overlay)
    }

    @Test
    fun `resolveLegacyThemeOverlay reads gliaChatStyle from the live theme`() {
        val overlay = themed(R.style.Test_Glia_Theme_WithLegacyChatStyle)
            .resolveLegacyThemeOverlay(R.style.Test_Glia_Legacy_ActivityStyleWithoutChatStyle)

        assertEquals(R.style.Test_Glia_Legacy_Overlay, overlay)
    }

    @Test
    fun `resolveLegacyThemeOverlay falls back to the overridden activity style resource`() {
        val overlay = themed(R.style.Theme_Glia_Internal)
            .resolveLegacyThemeOverlay(R.style.Test_Glia_Legacy_ActivityStyleWithChatStyle)

        assertEquals(R.style.Test_Glia_Legacy_Overlay, overlay)
    }

    @Test
    fun `resolveLegacyThemeOverlay prefers the live theme over the activity style resource`() {
        val overlay = themed(R.style.Test_Glia_Theme_WithContextLegacyChatStyle)
            .resolveLegacyThemeOverlay(R.style.Test_Glia_Legacy_ActivityStyleWithChatStyle)

        assertEquals(R.style.Test_Glia_Legacy_ContextOverlay, overlay)
    }

    @Test
    fun `resolveLegacyThemeOverlay returns null when gliaChatStyle declares no materialThemeOverlay`() {
        val overlay = themed(R.style.Test_Glia_Theme_WithLegacyChatStyleWithoutOverlay)
            .resolveLegacyThemeOverlay(R.style.Test_Glia_Legacy_ActivityStyleWithoutChatStyle)

        assertNull(overlay)
    }

    // endregion

    // region composition

    @Test
    fun `legacy overlay replaces only the items it declares`() {
        val context = themed(R.style.Test_Glia_Theme_WithLegacyChatStyle)

        context.theme.applyStyle(requireNotNull(context.resolveLegacyThemeOverlay()), true)

        assertEquals(R.color.glia_test_legacy_brand, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.drawable.glia_test_legacy_icon, context.resourceIdOf(R.attr.gliaIconAppBarBack))
        // untouched by the overlay
        assertEquals(R.drawable.ic_baseline_send, context.resourceIdOf(R.attr.gliaIconSendMessage))
        assertEquals(R.color.glia_dark_color, context.resourceIdOf(R.attr.gliaBaseDarkColor))
    }

    @Test
    fun `customization wins over the legacy overlay on overlapping attributes`() {
        val context = themed(R.style.Test_Glia_Theme_WithLegacyChatStyle)

        context.applyGliaThemeOverlays(customizationStyle = R.style.Test_Glia_Customization)

        // declared by both -> customization wins
        assertEquals(R.color.glia_test_customization_brand, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        // declared only by the legacy overlay -> still applied
        assertEquals(R.drawable.glia_test_legacy_icon, context.resourceIdOf(R.attr.gliaIconAppBarBack))
        // declared only by the customization -> applied
        assertEquals(R.drawable.glia_test_customization_icon, context.resourceIdOf(R.attr.gliaIconLeaveQueue))
        // declared by neither -> default
        assertEquals(R.drawable.ic_baseline_send, context.resourceIdOf(R.attr.gliaIconSendMessage))
    }

    @Test
    fun `customization applies without any legacy mechanism in play`() {
        val context = themed(R.style.Theme_Glia_Internal)

        context.applyGliaThemeOverlays(
            legacyActivityStyle = R.style.Test_Glia_Legacy_ActivityStyleWithoutChatStyle,
            customizationStyle = R.style.Test_Glia_Customization
        )

        assertEquals(R.color.glia_test_customization_brand, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.drawable.glia_test_customization_icon, context.resourceIdOf(R.attr.gliaIconLeaveQueue))
        assertEquals(R.drawable.ic_baseline_arrow_back, context.resourceIdOf(R.attr.gliaIconAppBarBack))
    }

    @Test
    fun `skipping the legacy overlay still applies the customization`() {
        val context = themed(R.style.Test_Glia_Theme_WithLegacyChatStyle)

        context.applyGliaThemeOverlays(
            includeLegacyOverlay = false,
            customizationStyle = R.style.Test_Glia_Customization
        )

        assertEquals(R.color.glia_test_customization_brand, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.drawable.glia_test_customization_icon, context.resourceIdOf(R.attr.gliaIconLeaveQueue))
        // the legacy overlay must not have been applied
        assertEquals(R.drawable.ic_baseline_arrow_back, context.resourceIdOf(R.attr.gliaIconAppBarBack))
    }

    // endregion

    // region activity hook

    /**
     * Why the SDK activities compose from `onApplyThemeResource` and not from `onCreate`.
     *
     * `Theme.Glia.Internal` declares every `glia*` attribute, so anything that force-applies the
     * activity's theme resource replaces the overlays wholesale. The framework does that once, in
     * `setTheme`, after `attachBaseContext`; `AppCompatDelegateImpl.updateResourcesConfiguration`
     * does it again whenever `AppCompatDelegate.mThemeResId` is set. Composing from
     * `onApplyThemeResource` is ordered after every such application, by construction.
     */
    @Test
    fun `composing before the activity theme is applied is undone by it`() {
        val context = ContextThemeWrapper(application, 0)

        // attachBaseContext position: the manifest theme has not been applied yet
        context.compose()
        assertEquals(R.color.glia_test_customization_brand, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))

        context.setTheme(R.style.Theme_Glia_Internal_Chat)

        assertEquals(R.color.glia_primary_color, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.drawable.ic_baseline_arrow_back, context.resourceIdOf(R.attr.gliaIconAppBarBack))
    }

    @Test
    fun `composing from onApplyThemeResource survives re-application of the activity theme`() {
        val context = object : ContextThemeWrapper(application, 0) {
            override fun onApplyThemeResource(theme: Resources.Theme, resid: Int, first: Boolean) {
                super.onApplyThemeResource(theme, resid, first)
                compose()
            }
        }

        context.setTheme(R.style.Theme_Glia_Internal_Chat)

        assertEquals(R.color.glia_test_customization_brand, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.drawable.glia_test_legacy_icon, context.resourceIdOf(R.attr.gliaIconAppBarBack))

        // What AppCompat does when a night-mode / locale update re-applies the theme resource.
        context.setTheme(R.style.Theme_Glia_Internal_Translucent)

        assertEquals(R.color.glia_test_customization_brand, context.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.drawable.glia_test_legacy_icon, context.resourceIdOf(R.attr.gliaIconAppBarBack))
    }

    private fun Context.compose() = applyGliaThemeOverlays(
        legacyActivityStyle = R.style.Test_Glia_Legacy_ActivityStyleWithChatStyle,
        customizationStyle = R.style.Test_Glia_Customization
    )

    // endregion

    // region attribute resolution

    @Test
    fun `gliaAttrResourceId returns the resource the theme points at`() {
        val context = themed(R.style.Theme_Glia_Internal)

        assertEquals(R.color.glia_primary_color, context.gliaAttrResourceId(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.drawable.ic_baseline_arrow_back, context.gliaAttrResourceId(R.attr.gliaIconAppBarBack))
    }

    @Test
    fun `gliaAttrResourceId returns null when the theme does not declare the attribute`() {
        val context = themed(R.style.Test_Glia_Theme_HostApp)

        assertNull(context.gliaAttrResourceId(R.attr.gliaIconSendMessage))
        assertEquals(R.drawable.ic_baseline_send, context.gliaAttrResourceId(R.attr.gliaIconSendMessage, R.drawable.ic_baseline_send))
    }

    @Test
    fun `gliaAttrDrawableRes resolves icon attributes and falls back when absent`() {
        val glia = themed(R.style.Theme_Glia_Internal)
        val host = themed(R.style.Test_Glia_Theme_HostApp)

        assertEquals(
            R.drawable.ic_baseline_close,
            glia.gliaAttrDrawableRes(R.attr.gliaIconLeaveQueue, R.drawable.ic_baseline_send)
        )
        assertEquals(
            R.drawable.ic_baseline_send,
            host.gliaAttrDrawableRes(R.attr.gliaIconLeaveQueue, R.drawable.ic_baseline_send)
        )
    }

    @Test
    fun `gliaAttrColor resolves a colour attribute and falls back when absent`() {
        val glia = themed(R.style.Theme_Glia_Internal)
        val host = themed(R.style.Test_Glia_Theme_HostApp)

        assertEquals(
            ContextCompat.getColor(glia, R.color.glia_primary_color),
            glia.gliaAttrColor(R.attr.gliaBrandPrimaryColor, R.color.glia_dark_color)
        )
        assertEquals(
            ContextCompat.getColor(host, R.color.glia_dark_color),
            host.gliaAttrColor(R.attr.gliaBrandPrimaryColor, R.color.glia_dark_color)
        )
    }

    /**
     * The `fontFamily` -> font-resource branch is not covered: asserting it would mean shipping a
     * binary font fixture in the published module's `debug` source set for one assertion, and the
     * branch is a single delegation to `ResourcesCompat.getFont`. What is worth locking is that
     * neither of the two "no typeface" cases produces one, since both are reached in production.
     */
    @Test
    fun `gliaAttrFont resolves to null unless the theme points at a font resource`() {
        val glia = themed(R.style.Theme_Glia_Internal)
        val familyName = themed(R.style.Theme_Glia_Internal)
            .apply { theme.applyStyle(R.style.Test_Glia_Customization_FontFamilyName, true) }

        assertNull(glia.gliaAttrFont())
        assertNull(familyName.gliaAttrFont())
        assertNull(familyName.gliaAttrResourceId(androidx.appcompat.R.attr.fontFamily))
    }

    @Test
    fun `gliaAttrBoolean reads the attribute and falls back to the given default`() {
        val glia = themed(R.style.Theme_Glia_Internal)
        val host = themed(R.style.Test_Glia_Theme_HostApp)
        val customized = themed(R.style.Theme_Glia_Internal)
            .apply { theme.applyStyle(R.style.Test_Glia_Customization_Booleans, true) }

        assertFalse(glia.gliaAttrBoolean(R.attr.whiteLabel))
        assertFalse(glia.gliaAttrBoolean(R.attr.gliaAlertDialogButtonUseVerticalAlignment))
        assertTrue(customized.gliaAttrBoolean(R.attr.whiteLabel))
        assertTrue(customized.gliaAttrBoolean(R.attr.gliaAlertDialogButtonUseVerticalAlignment))
        assertFalse(host.gliaAttrBoolean(R.attr.whiteLabel))
        assertTrue(host.gliaAttrBoolean(R.attr.whiteLabel, default = true))
    }

    @Test
    fun `the helpers read the composed theme, not the base one`() {
        val context = themed(R.style.Test_Glia_Theme_WithLegacyChatStyle)

        context.applyGliaThemeOverlays(customizationStyle = R.style.Test_Glia_Customization)

        assertEquals(
            R.color.glia_test_customization_brand,
            context.gliaAttrResourceId(R.attr.gliaBrandPrimaryColor)
        )
        assertEquals(
            R.drawable.glia_test_legacy_icon,
            context.gliaAttrDrawableRes(R.attr.gliaIconAppBarBack, R.drawable.ic_baseline_send)
        )
        assertEquals(
            R.drawable.glia_test_customization_icon,
            context.gliaAttrDrawableRes(R.attr.gliaIconLeaveQueue, R.drawable.ic_baseline_send)
        )
    }

    // endregion

    // region wrapWithGliaTheme

    @Test
    fun `wrapWithGliaTheme resolves every glia attribute on a plain host theme`() {
        val host = themed(R.style.Test_Glia_Theme_HostApp)

        assertEquals(0, host.resourceIdOf(R.attr.gliaIconSendMessage))

        val wrapped = host.wrapWithGliaTheme()

        assertEquals(R.drawable.ic_baseline_send, wrapped.resourceIdOf(R.attr.gliaIconSendMessage))
        assertEquals(R.drawable.ic_baseline_arrow_back, wrapped.resourceIdOf(R.attr.gliaIconAppBarBack))
        assertEquals(R.color.glia_dark_color, wrapped.resourceIdOf(R.attr.gliaBaseDarkColor))
        assertEquals(R.color.glia_primary_color, wrapped.resourceIdOf(R.attr.gliaBrandPrimaryColor))
    }

    @Test
    fun `wrapWithGliaTheme applies a gliaChatStyle declared by the host theme`() {
        val wrapped = themed(R.style.Test_Glia_Theme_WithContextLegacyChatStyle).wrapWithGliaTheme()

        assertEquals(R.color.glia_test_context_brand, wrapped.resourceIdOf(R.attr.gliaBrandPrimaryColor))
        assertEquals(R.drawable.ic_baseline_arrow_back, wrapped.resourceIdOf(R.attr.gliaIconAppBarBack))
    }

    @Test
    fun `wrapWithGliaTheme leaves the host theme untouched`() {
        val host = themed(R.style.Test_Glia_Theme_HostApp)

        host.wrapWithGliaTheme()

        assertEquals(0, host.resourceIdOf(R.attr.gliaIconSendMessage))
    }

    // endregion
}
