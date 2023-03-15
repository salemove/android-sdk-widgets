@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.call.ButtonBarTheme
import com.glia.widgets.view.unifiedui.theme.call.CallTheme

/**
 * Default theme for Call screen
 */
internal fun CallTheme(pallet: ColorPallet) = pallet.run {
    val header = CallHeaderTheme(this)

    val baseLightText = TextTheme(textColor = baseLightColorTheme)
    // must not have a background in default theme to imitate native dialler

    CallTheme(
        bottomText = baseLightText,
        buttonBar = ButtonBarTheme(badge = BadgeTheme(pallet)),
        duration = baseLightText,
        header = header,
        operator = baseLightText,
        topText = baseLightText,
        connect = CallEngagementStatesTheme(pallet)
    )
}

/**
 * Default theme for Call screen header
 */
private fun CallHeaderTheme(colorPallet: ColorPallet) = DefaultHeader(
    background = null, // must be null for default them to imitate native dialler
    lightColor = colorPallet.baseLightColorTheme,
    negative = colorPallet.systemNegativeColorTheme
)
