@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.call.CallTheme

/**
 * Default theme for Call screen
 */
internal fun CallDefaultTheme(pallet: ColorPallet) =
    CallTheme(
        background = null, // must be null for default theme to imitate native dialler
        bottomText = null,
        buttonBar = null,
        duration = null,
        header = CallHeader(pallet),
        operator = null,
        topText = null,
        connect = null
    )

/**
 * Default theme for Call screen header
 */
private fun CallHeader(colorPallet: ColorPallet) = DefaultHeader(
    background = null, // must be null for default them to imitate native dialler
    lightColor = colorPallet.baseLightColorTheme,
    negative = colorPallet.systemNegativeColorTheme
)
