@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.exstensions.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.*

/**
 * Default theme for Chat screen
 */
internal fun ChatDefaultTheme(pallet: ColorPallet): ChatTheme =
    ChatTheme(
        background = LayerTheme(fill = pallet.backgroundColorTheme),
        header = ChatHeader(pallet),
        operatorMessage = ChatOperatorMessage(pallet),
        visitorMessage = ChatVisitorMessage(pallet),
        connect = ChatEngagementStates(pallet),
        input = null,
        responseCard = ChatResponseCardTheme(pallet),
        audioUpgrade = MediaUpgradeDefaultTheme(pallet),
        videoUpgrade = MediaUpgradeDefaultTheme(pallet),
        bubble = null,
        attachmentsPopup = null,
        unreadIndicator = null,
        typingIndicator = null
    )

/**
 * Default theme for Audio and Video upgrade card
 */
private fun MediaUpgradeDefaultTheme(pallet: ColorPallet) = pallet.run {
    composeIfAtLeastOneNotNull(
        baseDarkColorTheme,
        baseNormalColorTheme,
        primaryColorTheme,
        baseLightColorTheme,
        baseShadeColorTheme
    ) {
        MediaUpgradeTheme(
            text = TextTheme(textColor = baseDarkColorTheme),
            description = TextTheme(textColor = baseNormalColorTheme),
            iconColor = primaryColorTheme,
            background = LayerTheme(
                fill = baseLightColorTheme,
                stroke = baseShadeColorTheme?.primaryColor
            )
        )
    }
}

/**
 * Default theme for Chat screen header
 */
private fun ChatHeader(colorPallet: ColorPallet) = colorPallet.run {
    DefaultHeader(
        background = primaryColorTheme,
        lightColor = baseLightColorTheme,
        negative = systemNegativeColorTheme
    )
}

/**
 * Default theme for Operator Message
 */
private fun ChatOperatorMessage(
    pallet: ColorPallet
): MessageBalloonTheme? = pallet.run {
    val userImage = UserImage(this)
    composeIfAtLeastOneNotNull(userImage, baseDarkColorTheme) {
        MessageBalloonTheme(
            text = TextTheme(textColor = baseDarkColorTheme),
            userImage = userImage
        )
    }
}

/**
 * Default theme for Visitor Message
 */
private fun ChatVisitorMessage(
    pallet: ColorPallet
): MessageBalloonTheme? = pallet.run {
    composeIfAtLeastOneNotNull(primaryColorTheme, baseDarkColorTheme, baseNormalColorTheme) {
        MessageBalloonTheme(
            background = LayerTheme(fill = primaryColorTheme),
            text = TextTheme(textColor = baseDarkColorTheme),
            status = TextTheme(textColor = baseNormalColorTheme)
        )
    }
}

/**
 * Default theme for Response Card
 */
private fun ChatResponseCardTheme(pallet: ColorPallet): ResponseCardTheme? {
    return pallet.run {
        composeIfAtLeastOneNotNull(primaryColorTheme, baseLightColorTheme, baseDarkColorTheme) {
            ResponseCardTheme(
                option = ResponseCardOptionTheme(normal = NeutralDefaultButton(pallet)),
                background = LayerTheme(
                    fill = baseLightColorTheme,
                    stroke = primaryColorTheme?.primaryColor
                ),
                text = TextTheme(textColor = baseDarkColorTheme)
            )
        }
    }
}