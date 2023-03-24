@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.extensions.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

/**
 * Default theme for Neutral Button
 */
internal fun NeutralDefaultButtonTheme(pallet: ColorPallet) =
    DefaultButtonTheme(text = pallet.baseDarkColorTheme)

/**
 * Default theme for Positive Button
 */
internal fun PositiveDefaultButtonTheme(pallet: ColorPallet) = pallet.run {
    DefaultButtonTheme(text = baseLightColorTheme, background = primaryColorTheme)
}

/**
 * Default theme for Negative Button
 */
internal fun NegativeDefaultButtonTheme(pallet: ColorPallet) = pallet.run {
    DefaultButtonTheme(text = baseLightColorTheme, background = systemNegativeColorTheme)
}

private fun DefaultButtonTheme(text: ColorTheme? = null, background: ColorTheme? = null) =
    composeIfAtLeastOneNotNull(text, background) {
        ButtonTheme(
            background = LayerTheme(fill = background),
            text = TextTheme(textColor = text)
        )
    }
