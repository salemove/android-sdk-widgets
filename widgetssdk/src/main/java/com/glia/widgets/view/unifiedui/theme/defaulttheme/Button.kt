@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.exstensions.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

/**
 * Default theme for Neutral Button
 */
internal fun NeutralDefaultButton(pallet: ColorPallet) =
    DefaultButton(text = pallet.baseDarkColorTheme)

/**
 * Default theme for Positive Button
 */
internal fun PositiveDefaultButton(pallet: ColorPallet) = pallet.run {
    DefaultButton(text = baseLightColorTheme, background = primaryColorTheme)
}

/**
 * Default theme for Negative Button
 */
internal fun NegativeDefaultButton(pallet: ColorPallet) = pallet.run {
    DefaultButton(text = baseLightColorTheme, background = systemNegativeColorTheme)
}

private fun DefaultButton(text: ColorTheme? = null, background: ColorTheme? = null) =
    composeIfAtLeastOneNotNull(text, background) {
        ButtonTheme(
            background = LayerTheme(fill = background),
            text = TextTheme(textColor = text)
        )
    }