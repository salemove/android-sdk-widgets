@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.*


/**
 * Create Default theme for [com.glia.widgets.view.header.AppBarView]
 * @see [HeaderTheme]
 */
internal fun DefaultHeader(
    background: ColorTheme?,
    lightColor: ColorTheme?,
    negative: ColorTheme?
): HeaderTheme? = composeIfAtLeastOneNotNull(background, lightColor, negative) {
    HeaderTheme(
        text = TextTheme(textColor = lightColor),
        background = LayerTheme(fill = background),
        backButton = ButtonTheme(iconColor = lightColor),
        closeButton = ButtonTheme(iconColor = lightColor),
        endButton = ButtonTheme(
            text = TextTheme(textColor = lightColor),
            background = LayerTheme(fill = negative)
        )
    )
}

/**
 * Default theme for Chat screen header
 */
internal fun PrimaryColorHeaderTheme(colorPallet: ColorPallet) = colorPallet.run {
    composeIfAtLeastOneNotNull(primaryColorTheme, baseLightColorTheme, systemNegativeColorTheme) {
        DefaultHeader(
            background = primaryColorTheme,
            lightColor = baseLightColorTheme,
            negative = systemNegativeColorTheme
        )
    }
}
