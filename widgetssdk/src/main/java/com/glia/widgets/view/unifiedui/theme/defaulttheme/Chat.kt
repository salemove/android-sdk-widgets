@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme

/**
 * Default theme for Chat screen
 */
internal fun ChatDefaultTheme(pallet: ColorPallet): ChatTheme =
    ChatTheme(
        background = LayerTheme(fill = pallet.backgroundColorTheme),
        header = ChatHeader(pallet),
        operatorMessage = null,
        visitorMessage = null,
        connect = null,
        input = null,
        responseCard = null,
        audioUpgrade = null,
        videoUpgrade = null,
        bubble = null,
        attachmentsPopup = null,
        unreadIndicator = null,
        typingIndicator = null
    )

/**
 * Default theme for Chat screen header
 */
private fun ChatHeader(colorPallet: ColorPallet) = DefaultHeader(
    background = colorPallet.primaryColorTheme,
    lightColor = colorPallet.baseLightColorTheme,
    negative = colorPallet.systemNegativeColorTheme
)