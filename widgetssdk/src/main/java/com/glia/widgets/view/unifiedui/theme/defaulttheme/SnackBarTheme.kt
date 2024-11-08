@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.SnackBarTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme

/**
 * Default [SnackBarTheme] generated for [ColorPallet]
 */
internal fun DefaultSnackBarTheme(pallet: ColorPallet?): SnackBarTheme? = pallet?.run {
    SnackBarTheme(background = darkColorTheme, text = lightColorTheme)
}

/**
 * [SnackBarTheme] generated for Call Screen
 */
internal fun CallSnackBarTheme(pallet: ColorPallet?): SnackBarTheme? = pallet?.run {
    SnackBarTheme(background = lightColorTheme, text = darkColorTheme)
}

internal fun SnackBarTheme(background: ColorTheme?, text: ColorTheme?): SnackBarTheme? = composeIfAtLeastOneNotNull(background, text) {
    SnackBarTheme(backgroundColorTheme = background, textColorTheme = text)
}
