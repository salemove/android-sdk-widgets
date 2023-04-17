@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme

/**
 * Default theme for Alert
 */
internal fun AlertTheme(pallet: ColorPallet): AlertTheme = pallet.run {
    AlertTheme(
        title = BaseDarkColorTextTheme(this),
        titleImageColor = primaryColorTheme,
        message = BaseDarkColorTextTheme(this),
        backgroundColor = backgroundColorTheme,
        closeButtonColor = baseNormalColorTheme,
        positiveButton = PositiveDefaultButtonTheme(this),
        negativeButton = NegativeDefaultButtonTheme(this)
    )
}
