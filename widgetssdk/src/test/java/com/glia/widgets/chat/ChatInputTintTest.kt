package com.glia.widgets.chat

import android.content.Context
import android.content.res.ColorStateList
import android.view.ContextThemeWrapper
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.glia.widgets.R
import com.glia.widgets.helper.applyGliaThemeOverlays
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * `ChatView` used to re-apply the chat input's colours in code on top of what `chat_view.xml`
 * already resolved. Those code paths are gone, so the state list resources are now the only thing
 * that paints the input row - and the send button's one had to be re-pointed from the coarse
 * `gliaBrandPrimaryColor` to the precise `gliaSendMessageButtonTintColor` for that to be lossless.
 *
 * Neither the enabled/disabled split nor the precise attribute is visible to Paparazzi, so it is
 * locked here.
 */
@RunWith(RobolectricTestRunner::class)
internal class ChatInputTintTest {

    private val enabled = intArrayOf(android.R.attr.state_enabled)
    private val disabled = intArrayOf(-android.R.attr.state_enabled)

    private fun stateListOf(@ColorRes colorRes: Int, @StyleRes customizationStyle: Int? = null): ColorStateList {
        val context: Context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_Glia_Internal)
        customizationStyle?.also { context.applyGliaThemeOverlays(customizationStyle = it) }

        return requireNotNull(ContextCompat.getColorStateList(context, colorRes))
    }

    private fun Context.color(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

    private val application: Context get() = RuntimeEnvironment.getApplication()

    @Test
    fun `send button tint defaults to the brand colour`() {
        val stateList = stateListOf(R.color.chat_send_button_color_state_list)

        assertEquals(application.color(R.color.glia_primary_color), stateList.getColorForState(enabled, 0))
        assertEquals(application.color(R.color.glia_shade_color), stateList.getColorForState(disabled, 0))
    }

    @Test
    fun `send button tint follows gliaSendMessageButtonTintColor without the brand colour changing`() {
        val stateList = stateListOf(
            R.color.chat_send_button_color_state_list,
            R.style.Test_Glia_Customization_SendButtonTint
        )

        assertEquals(application.color(R.color.glia_test_customization_brand), stateList.getColorForState(enabled, 0))
        assertEquals(application.color(R.color.glia_shade_color), stateList.getColorForState(disabled, 0))
    }

    @Test
    fun `send button tint follows the brand colour when only that is customized`() {
        val stateList = stateListOf(R.color.chat_send_button_color_state_list, R.style.Test_Glia_Customization)

        assertEquals(application.color(R.color.glia_test_customization_brand), stateList.getColorForState(enabled, 0))
    }

    @Test
    fun `input text tint splits base dark and base shade`() {
        val stateList = stateListOf(R.color.chat_input_text_color_state_list)

        assertEquals(application.color(R.color.glia_dark_color), stateList.getColorForState(enabled, 0))
        assertEquals(application.color(R.color.glia_shade_color), stateList.getColorForState(disabled, 0))
    }

    @Test
    fun `input hint and attachment button tint splits base normal and base shade`() {
        val stateList = stateListOf(R.color.chat_input_hint_and_attachment_button_color_state_list)

        assertEquals(application.color(R.color.glia_normal_color), stateList.getColorForState(enabled, 0))
        assertEquals(application.color(R.color.glia_shade_color), stateList.getColorForState(disabled, 0))
    }
}
