@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import android.graphics.Color
import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

/**
 * Default theme for Neutral Button
 */
internal fun NeutralDefaultButtonTheme(pallet: ColorPallet) =
    DefaultButtonTheme(text = pallet.darkColorTheme)

/**
 * Default theme for Positive Button
 */
internal fun PositiveDefaultButtonTheme(pallet: ColorPallet) = pallet.run {
    DefaultButtonTheme(text = lightColorTheme, background = primaryColorTheme)
}

/**
 * Default theme for Negative Button
 */
internal fun NegativeDefaultButtonTheme(pallet: ColorPallet) = pallet.run {
    DefaultButtonTheme(text = lightColorTheme, background = negativeColorTheme)
}

/**
 * Default theme for Negative Neutral Button
 */
internal fun NegativeNeutralDefaultButtonTheme(pallet: ColorPallet) = pallet.run {
    OutlinedButtonTheme(
        text = negativeColorTheme,
        background = lightColorTheme,
        stroke = shadeColorTheme,
    )
}

/**
 * Default theme for Link Button
 */
internal fun LinkDefaultButtonTheme(pallet: ColorPallet) = pallet.run {
    ButtonTheme(
        background = LayerTheme(fill = ColorTheme(Color.TRANSPARENT)),
        text = TextTheme(textColor = primaryColorTheme),
        elevation = 0f
    )
}

/**
 * Default theme for Outlined Button
 */
internal fun OutlinedButtonTheme(
    text: ColorTheme?,
    stroke: ColorTheme?,
    background: ColorTheme? = null,
): ButtonTheme? = composeIfAtLeastOneNotNull(text, stroke, background) {
    ButtonTheme(
        text = TextTheme(textColor = text),
        background = LayerTheme(fill = background, stroke = stroke?.primaryColor),
        iconColor = null,
        elevation = 0f,
        shadowColor = null
    )
}

/**
 * Default theme for GVA Button
 */
internal fun GvaDefaultButtonTheme(pallet: ColorPallet) = pallet.run {
    DefaultButtonTheme(text = darkColorTheme, background = lightColorTheme)
}

private fun DefaultButtonTheme(
    text: ColorTheme? = null,
    background: ColorTheme? = null,
    iconColor: ColorTheme? = null
) = composeIfAtLeastOneNotNull(text, background, iconColor) {
    ButtonTheme(
        background = LayerTheme(fill = background),
        text = TextTheme(textColor = text),
        iconColor = iconColor ?: text
    )
}
