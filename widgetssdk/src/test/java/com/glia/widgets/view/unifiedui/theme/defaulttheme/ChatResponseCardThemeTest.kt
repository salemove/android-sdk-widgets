package com.glia.widgets.view.unifiedui.theme.defaulttheme

import android.graphics.Color
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.ResponseCardOptionTheme
import com.glia.widgets.view.unifiedui.theme.chat.ResponseCardTheme
import org.junit.Assert.assertEquals
import org.junit.Test

internal class ChatResponseCardThemeTest {

    private val dark = ColorTheme(Color.BLACK)
    private val light = ColorTheme(Color.WHITE)
    private val neutral = ColorTheme(Color.GRAY)
    private val primary = ColorTheme(Color.BLUE)

    private val pallet = ColorPallet(
        darkColorTheme = dark,
        lightColorTheme = light,
        neutralColorTheme = neutral,
        normalColorTheme = ColorTheme(Color.DKGRAY),
        shadeColorTheme = ColorTheme(Color.LTGRAY),
        primaryColorTheme = primary,
        secondaryColorTheme = ColorTheme(Color.CYAN),
        negativeColorTheme = ColorTheme(Color.RED)
    )

    @Test
    fun `global colors paint the whole card, options included, like the XML defaults do`() {
        val expected = ResponseCardTheme(
            background = LayerTheme(fill = light, stroke = primary.primaryColor),
            option = ResponseCardOptionTheme(
                normal = ButtonTheme(
                    background = LayerTheme(fill = neutral),
                    text = TextTheme(textColor = dark),
                    iconColor = dark
                )
            ),
            text = TextTheme(textColor = dark)
        )

        assertEquals(expected, ChatTheme(pallet).responseCard)
    }
}
