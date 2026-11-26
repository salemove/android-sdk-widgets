package com.glia.widgets.view.button

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.glia.widgets.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Locks the survey option button's state-driven theming, which replaced
 * `GliaSurveyOptionButton.applyView()` and the `OptionButtonConfiguration` it read.
 *
 * The colours asserted here are the ones the deleted configuration hard-coded, so this is also the
 * regression net for "the default survey rendering did not change".
 */
@RunWith(RobolectricTestRunner::class)
internal class GliaSurveyOptionButtonTest {

    private lateinit var context: Context
    private lateinit var button: GliaSurveyOptionButton

    @Before
    fun setUp() {
        context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_Glia_Internal)
        button = GliaSurveyOptionButton(context, null)
    }

    private fun color(@ColorRes colorRes: Int): Int = ContextCompat.getColor(context, colorRes)

    private val currentBackground: Int get() = button.backgroundTintList!!.getColorForState(button.drawableState, 0)
    private val currentStroke: Int get() = button.strokeColor!!.getColorForState(button.drawableState, 0)
    private val currentText: Int get() = button.textColors.getColorForState(button.drawableState, 0)

    @Test
    fun `unselected button uses the light fill, the dark label and a 30 percent normal border`() {
        assertEquals(color(R.color.glia_light_color), currentBackground)
        assertEquals(color(R.color.glia_dark_color), currentText)
        assertEquals(color(R.color.glia_normal_color_opacity_30), currentStroke)
    }

    @Test
    fun `selected button uses the brand fill and border with the light label`() {
        button.isSelected = true

        assertEquals(color(R.color.glia_primary_color), currentBackground)
        assertEquals(color(R.color.glia_primary_color), currentStroke)
        assertEquals(color(R.color.glia_light_color), currentText)
    }

    @Test
    fun `error turns the border negative and leaves the fill and label alone`() {
        button.isError = true

        assertEquals(color(R.color.glia_negative_color), currentStroke)
        assertEquals(color(R.color.glia_light_color), currentBackground)
        assertEquals(color(R.color.glia_dark_color), currentText)
    }

    @Test
    fun `error wins over selection on the border while selection still owns the fill and label`() {
        button.isSelected = true
        button.isError = true

        assertEquals(color(R.color.glia_negative_color), currentStroke)
        assertEquals(color(R.color.glia_primary_color), currentBackground)
        assertEquals(color(R.color.glia_light_color), currentText)
    }

    @Test
    fun `isError is backed by the activated drawable state so the state lists can react to it`() {
        assertFalse(button.isError)
        assertFalse(button.isActivated)

        button.isError = true

        assertTrue(button.isActivated)

        button.isError = false

        assertFalse(button.isActivated)
        assertFalse(button.isError)
    }

    @Test
    fun `a customized brand colour reaches the selected state`() {
        // `Test.Glia.Customization` stands in for a redefined `GliaTheme`; see `src/debug/res`.
        context.theme.applyStyle(R.style.Test_Glia_Customization, true)
        button = GliaSurveyOptionButton(context, null)
        button.isSelected = true

        assertEquals(color(R.color.glia_test_customization_brand), currentBackground)
    }
}
