@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.call.BarButtonStatesTheme
import com.glia.widgets.view.unifiedui.theme.call.BarButtonStyleTheme
import com.glia.widgets.view.unifiedui.theme.call.ButtonBarTheme
import com.glia.widgets.view.unifiedui.theme.call.CallTheme

/**
 * Default theme for Call screen
 */
internal fun CallTheme(pallet: ColorPallet) = pallet.run {
    val header = CallHeaderTheme(this)

    // must not have a background in default theme to imitate native dialler
    CallTheme(
        bottomText = BaseLightColorTextTheme(this),
        buttonBar = ButtonBarTheme(pallet),
        duration = BaseLightColorTextTheme(this),
        header = header,
        operator = BaseLightColorTextTheme(this),
        topText = BaseLightColorTextTheme(this),
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

private fun ButtonBarTheme(pallet: ColorPallet) = ButtonBarTheme(
    badge = BadgeTheme(pallet),
    chatButton = CallButtonBarButtonTheme(pallet),
    minimizeButton = CallButtonBarButtonTheme(pallet),
    muteButton = CallButtonBarButtonTheme(pallet),
    speakerButton = CallButtonBarButtonTheme(pallet),
    videoButton = CallButtonBarButtonTheme(pallet)
)

private fun CallButtonBarButtonTheme(pallet: ColorPallet) = pallet.run {
    composeIfAtLeastOneNotNull(baseLightColorTheme) {
        BarButtonStatesTheme(
            disabled = BarButtonStyleTheme(
                imageColor = baseLightColorTheme?.withAlpha(20f),
                title = BaseLightColorTextTheme(this)
            ),
            enabled = BarButtonStyleTheme(
                imageColor = baseLightColorTheme,
                title = BaseLightColorTextTheme(this)
            ),
            activated = BarButtonStyleTheme(
                imageColor = baseDarkColorTheme,
                title = BaseLightColorTextTheme(this)
            )
        )
    }
}
