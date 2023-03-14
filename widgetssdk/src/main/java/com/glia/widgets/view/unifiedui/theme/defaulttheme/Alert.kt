@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

/**
 * Default theme for Alert
 */
internal fun AlertTheme(pallet: ColorPallet): AlertTheme = pallet.run {
    AlertTheme(
        title = TextTheme(textColor = baseDarkColorTheme),
        titleImageColor = primaryColorTheme,
        message = TextTheme(textColor = baseDarkColorTheme),
        backgroundColor = backgroundColorTheme,
        closeButtonColor = baseNormalColorTheme,
        positiveButton = PositiveDefaultButtonTheme(this),
        negativeButton = NegativeDefaultButtonTheme(this)
    )
}
