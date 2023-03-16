@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.extensions.composeIfAtLeastOneNotNull
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
        header = ChatHeaderTheme(pallet),
        operatorMessage = ChatOperatorMessageTheme(pallet),
        visitorMessage = ChatVisitorMessageTheme(pallet),
        connect = ChatEngagementStatesTheme(pallet),
        input = ChatInputTheme(pallet),
        responseCard = ChatResponseCardTheme(pallet),
        audioUpgrade = MediaUpgradeTheme(pallet),
        videoUpgrade = MediaUpgradeTheme(pallet),
        bubble = BubbleTheme(pallet),
        attachmentsPopup = ChatAttachmentsPopupTheme(pallet),
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
            text = TextTheme(textColor = baseDarkColorTheme),
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
 * Default theme for Chat screen header
 */
private fun ChatHeaderTheme(colorPallet: ColorPallet) = colorPallet.run {
    DefaultHeader(
        background = primaryColorTheme,
        lightColor = baseLightColorTheme,
        negative = systemNegativeColorTheme
    )
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
            text = TextTheme(textColor = baseDarkColorTheme),
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
                option = ResponseCardOptionTheme(normal = NeutralDefaultButtonTheme(this)),
                background = LayerTheme(
                    fill = baseLightColorTheme,
                    stroke = primaryColorTheme?.primaryColor
                ),
                text = TextTheme(textColor = baseDarkColorTheme)
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
                text = TextTheme(textColor = baseDarkColorTheme),
                placeholder = TextTheme(textColor = baseNormalColorTheme),
                divider = baseShadeColorTheme,
                sendButton = ButtonTheme(iconColor = primaryColorTheme),
            )
        }
    }
}

/**
 * Default theme for Attachments popup
 */
private fun ChatAttachmentsPopupTheme(pallet: ColorPallet): AttachmentsPopupTheme? =
    pallet.run {
        composeIfAtLeastOneNotNull(baseDarkColorTheme, baseShadeColorTheme) {
            val attachmentItem = AttachmentItemTheme(
                text = TextTheme(textColor = baseDarkColorTheme),
                iconColor = baseDarkColorTheme
            )
            AttachmentsPopupTheme(
                photoLibrary = attachmentItem,
                takePhoto = attachmentItem,
                browse = attachmentItem,
                dividerColor = baseShadeColorTheme
            )
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
