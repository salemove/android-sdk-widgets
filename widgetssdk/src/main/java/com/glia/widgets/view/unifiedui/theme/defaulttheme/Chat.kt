@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme
import com.glia.widgets.view.unifiedui.theme.chat.InputTheme
import com.glia.widgets.view.unifiedui.theme.chat.MediaUpgradeTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme
import com.glia.widgets.view.unifiedui.theme.chat.ResponseCardOptionTheme
import com.glia.widgets.view.unifiedui.theme.chat.ResponseCardTheme
import com.glia.widgets.view.unifiedui.theme.chat.UnreadIndicatorTheme

/**
 * Default theme for Chat screen
 */
internal fun ChatTheme(pallet: ColorPallet): ChatTheme =
    ChatTheme(
        background = LayerTheme(fill = pallet.baseLightColorTheme),
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
        newMessagesDividerTextTheme = TextTheme(textColor = pallet.primaryColorTheme),
        gva = GvaTheme(pallet)
    )

/**
 * Default theme for Audio and Video upgrade card
 */
private fun MediaUpgradeTheme(pallet: ColorPallet) = pallet.run {
    composeIfAtLeastOneNotNull(
        baseDarkColorTheme,
        baseNormalColorTheme,
        primaryColorTheme,
        baseLightColorTheme,
        baseShadeColorTheme
    ) {
        MediaUpgradeTheme(
            text = BaseDarkColorTextTheme(this),
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
 * Default theme for Operator Message
 */
private fun ChatOperatorMessageTheme(
    pallet: ColorPallet
): MessageBalloonTheme? = pallet.run {
    val userImage = UserImageTheme(this)
    composeIfAtLeastOneNotNull(userImage, baseDarkColorTheme, baseNeutralColorTheme) {
        MessageBalloonTheme(
            text = BaseDarkColorTextTheme(this),
            userImage = userImage,
            background = LayerTheme(
                fill = baseNeutralColorTheme
            )
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
            status = TextTheme(textColor = baseNormalColorTheme),
            error = TextTheme(textColor = systemNegativeColorTheme)
        )
    }
}

/**
 * Default theme for Response Card
 */
private fun ChatResponseCardTheme(pallet: ColorPallet): ResponseCardTheme? {
    return pallet.run {
        composeIfAtLeastOneNotNull(primaryColorTheme, baseNeutralColorTheme, baseDarkColorTheme) {
            ResponseCardTheme(
                option = ResponseCardOptionTheme(normal = NeutralDefaultButtonTheme(this)),
                background = LayerTheme(
                    fill = baseNeutralColorTheme,
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
        composeIfAtLeastOneNotNull(
            baseDarkColorTheme,
            baseNormalColorTheme,
            primaryColorTheme,
            baseShadeColorTheme
        ) {
            InputTheme(
                text = BaseDarkColorTextTheme(this),
                placeholder = BaseNormalColorTextTheme(pallet),
                divider = baseShadeColorTheme,
                sendButton = ButtonTheme(iconColor = primaryColorTheme),
                mediaButton = ButtonTheme(iconColor = baseNormalColorTheme)
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
