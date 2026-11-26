package com.glia.widgets.view.unifiedui

import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.glia.widgets.R
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.call.BarButtonStatesTheme
import com.glia.widgets.view.unifiedui.theme.call.BarButtonStyleTheme
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class BarButtonStatesThemeTest {

    private val enabled = intArrayOf(android.R.attr.state_enabled)
    private val disabled = intArrayOf(-android.R.attr.state_enabled)

    private val chatButton: FloatingActionButton = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_Glia_Internal)
        .let { LayoutInflater.from(it).inflate(R.layout.call_buttons_layout, FrameLayout(it), false) }
        .findViewById(R.id.chat_button)

    @Test
    fun `a theme for the disabled state keeps the layout colours of the enabled state`() {
        val imageColor = (chatButton.supportImageTintList ?: chatButton.imageTintList)?.getColorForState(enabled, 0)
        val backgroundColor = (chatButton.supportBackgroundTintList ?: chatButton.backgroundTintList)?.getColorForState(enabled, 0)

        chatButton.applyBarButtonStatesTheme(
            BarButtonStatesTheme(disabled = BarButtonStyleTheme(background = ColorTheme(Color.RED), imageColor = ColorTheme(Color.YELLOW)))
        )

        assertEquals(imageColor, chatButton.supportImageTintList?.getColorForState(enabled, 0))
        assertEquals(backgroundColor, chatButton.supportBackgroundTintList?.getColorForState(enabled, 0))
        assertEquals(Color.YELLOW, chatButton.supportImageTintList?.getColorForState(disabled, 0))
        assertEquals(Color.RED, chatButton.supportBackgroundTintList?.getColorForState(disabled, 0))
    }
}
