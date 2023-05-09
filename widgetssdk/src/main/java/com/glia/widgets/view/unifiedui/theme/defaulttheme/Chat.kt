@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.*

/**
 * Default theme for Chat screen
 */
internal fun ChatTheme(pallet: ColorPallet): ChatTheme =
    ChatTheme(
        background = LayerTheme(fill = pallet.backgroundColorTheme),
        header = PrimaryColorHeaderTheme(pallet),
        operatorMessage = ChatOperatorMessageTheme(pallet),
        visitorMessage = ChatVisitorMessageTheme(pallet),
        connect = ChatEngagementStatesTheme(pallet),
        input = ChatInputTheme(pallet),
        responseCard = ChatResponseCardTheme(pallet),
        audioUpgrade = MediaUpgradeTheme(pallet),
        videoUpgrade = MediaUpgradeTheme(pallet),
        bubble = BubbleTheme(pallet),
        attachmentsPopup = DefaultAttachmentsPopupTheme(pallet),
        unreadIndicator = ChatUnreadIndicatorTheme(pallet),
        typingIndicator = pallet.primaryColorTheme,
        newMessagesDividerColorTheme = pallet.primaryColorTheme,
        newMessagesDividerTextTheme = TextTheme(textColor = pallet.primaryColorTheme)
    )

/**
 * Default theme for Audio and Video upgrade card
 */
private fun MediaUpgradeTheme(pallet: ColorPallet) = pallet.run {
    composeIfAtLeastOneNotNull(
        baseDarkColorTheme,
        baseNormalColorTheme,
        primaryColorTheme,
        backgroundColorTheme,
        baseShadeColorTheme
    ) {
        MediaUpgradeTheme(
            text = BaseDarkColorTextTheme(this),
            description = TextTheme(textColor = baseNormalColorTheme),
            iconColor = primaryColorTheme,
            background = LayerTheme(
                fill = backgroundColorTheme,
                stroke = baseShadeColorTheme?.primaryColor
            )
        )
    }
}

/**
 * Default theme for Operator Message
 */
private fun ChatOperatorMessageTheme(
    pallet: ColorPallet
): MessageBalloonTheme? = pallet.run {
    val userImage = UserImageTheme(this)
    composeIfAtLeastOneNotNull(userImage, baseDarkColorTheme) {
        MessageBalloonTheme(
            text = BaseDarkColorTextTheme(this),
            userImage = userImage
        )
    }
}

/**
 * Default theme for Visitor Message
 */
private fun ChatVisitorMessageTheme(
    pallet: ColorPallet
): MessageBalloonTheme? = pallet.run {
    composeIfAtLeastOneNotNull(primaryColorTheme, baseLightColorTheme, baseNormalColorTheme) {
        MessageBalloonTheme(
            background = LayerTheme(fill = primaryColorTheme),
            text = BaseLightColorTextTheme(this),
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
                option = ResponseCardOptionTheme(normal = NeutralDefaultButtonTheme(this)),
                background = LayerTheme(
                    fill = baseLightColorTheme,
                    stroke = primaryColorTheme?.primaryColor
                ),
                text = BaseDarkColorTextTheme(this)
            )
        }
    }
}

/**
 * Default theme for Input
 */
private fun ChatInputTheme(pallet: ColorPallet): InputTheme? {
    return pallet.run {
        composeIfAtLeastOneNotNull(baseDarkColorTheme, baseNormalColorTheme, primaryColorTheme, baseShadeColorTheme) {
            InputTheme(
                text = BaseDarkColorTextTheme(this),
                placeholder = BaseNormalColorTextTheme(pallet),
                divider = baseShadeColorTheme,
                sendButton = ButtonTheme(iconColor = primaryColorTheme),
            )
        }
    }
}

/**
 * Default theme for Unread messages indicator
 */
private fun ChatUnreadIndicatorTheme(pallet: ColorPallet): UnreadIndicatorTheme? =
    pallet.run {
        val bubble = BubbleTheme(this)
        composeIfAtLeastOneNotNull(bubble, baseLightColorTheme) {
            UnreadIndicatorTheme(
                background = baseLightColorTheme,
                bubble = bubble
            )
        }
    }
