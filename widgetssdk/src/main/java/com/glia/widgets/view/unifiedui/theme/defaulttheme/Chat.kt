@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme
import com.glia.widgets.view.unifiedui.theme.chat.FilePreviewTheme
import com.glia.widgets.view.unifiedui.theme.chat.FileUploadBarTheme
import com.glia.widgets.view.unifiedui.theme.chat.InputTheme
import com.glia.widgets.view.unifiedui.theme.chat.MediaUpgradeTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme
import com.glia.widgets.view.unifiedui.theme.chat.ResponseCardOptionTheme
import com.glia.widgets.view.unifiedui.theme.chat.ResponseCardTheme
import com.glia.widgets.view.unifiedui.theme.chat.UnreadIndicatorTheme
import com.glia.widgets.view.unifiedui.theme.chat.UploadFileTheme

/**
 * Default theme for Chat screen
 */
internal fun ChatTheme(pallet: ColorPallet): ChatTheme = ChatTheme(
    background = LayerTheme(fill = pallet.lightColorTheme),
    header = PrimaryColorHeaderTheme(pallet),
    operatorMessage = ChatOperatorMessageTheme(pallet),
    visitorMessage = ChatVisitorMessageTheme(pallet),
    connect = ChatEngagementStatesTheme(pallet),
    input = ChatInputTheme(pallet),
    inputDisabled = ChatInputDisabledTheme(pallet),
    responseCard = ChatResponseCardTheme(pallet),
    audioUpgrade = MediaUpgradeTheme(pallet),
    videoUpgrade = MediaUpgradeTheme(pallet),
    bubble = BubbleTheme(pallet),
    attachmentsPopup = DefaultAttachmentsPopupTheme(pallet),
    unreadIndicator = ChatUnreadIndicatorTheme(pallet),
    typingIndicator = pallet.primaryColorTheme,
    newMessagesDividerColorTheme = pallet.primaryColorTheme,
    newMessagesDividerTextTheme = TextTheme(textColor = pallet.primaryColorTheme),
    gva = GvaTheme(pallet),
    secureMessaging = SecureMessagingTheme(pallet)
)

/**
 * Default theme for Audio and Video upgrade card
 */
private fun MediaUpgradeTheme(pallet: ColorPallet) = pallet.run {
    composeIfAtLeastOneNotNull(
        darkColorTheme,
        normalColorTheme,
        primaryColorTheme,
        lightColorTheme,
        shadeColorTheme
    ) {
        MediaUpgradeTheme(
            text = BaseDarkColorTextTheme(this),
            description = TextTheme(textColor = normalColorTheme),
            iconColor = primaryColorTheme,
            background = LayerTheme(
                fill = lightColorTheme,
                stroke = shadeColorTheme?.primaryColor
            )
        )
    }
}

/**
 * Default theme for Operator Message
 */
private fun ChatOperatorMessageTheme(pallet: ColorPallet): MessageBalloonTheme? = pallet.run {
    val userImage = UserImageTheme(this)
    composeIfAtLeastOneNotNull(userImage, darkColorTheme, neutralColorTheme) {
        MessageBalloonTheme(
            text = BaseDarkColorTextTheme(this),
            userImage = userImage,
            background = LayerTheme(
                fill = neutralColorTheme
            )
        )
    }
}

/**
 * Default theme for Visitor Message
 */
private fun ChatVisitorMessageTheme(pallet: ColorPallet): MessageBalloonTheme? = pallet.run {
    composeIfAtLeastOneNotNull(primaryColorTheme, lightColorTheme, normalColorTheme) {
        MessageBalloonTheme(
            background = LayerTheme(fill = primaryColorTheme),
            text = BaseLightColorTextTheme(this),
            status = TextTheme(textColor = normalColorTheme),
            error = TextTheme(textColor = negativeColorTheme)
        )
    }
}

/**
 * Default theme for Response Card
 */
private fun ChatResponseCardTheme(pallet: ColorPallet): ResponseCardTheme? = pallet.run {
    composeIfAtLeastOneNotNull(primaryColorTheme, neutralColorTheme, darkColorTheme) {
        ResponseCardTheme(
            option = ResponseCardOptionTheme(normal = NeutralDefaultButtonTheme(this)),
            background = LayerTheme(
                fill = neutralColorTheme,
                stroke = primaryColorTheme?.primaryColor
            ),
            text = BaseDarkColorTextTheme(this)
        )
    }
}

/**
 * Default theme for Input
 */
private fun ChatInputTheme(pallet: ColorPallet): InputTheme? = pallet.run {
    composeIfAtLeastOneNotNull(
        darkColorTheme,
        normalColorTheme,
        primaryColorTheme,
        shadeColorTheme,
        negativeColorTheme,
        lightColorTheme
    ) {
        InputTheme(
            background = LayerTheme(fill = lightColorTheme),
            text = BaseDarkColorTextTheme(this),
            placeholder = BaseNormalColorTextTheme(this),
            divider = shadeColorTheme,
            sendButton = ButtonTheme(iconColor = primaryColorTheme),
            mediaButton = ButtonTheme(iconColor = normalColorTheme),
            fileUploadBar = FileUploadBarTheme(
                filePreview = FilePreviewTheme(
                    text = BaseLightColorTextTheme(this),
                    errorIcon = negativeColorTheme,
                    background = LayerTheme(neutralColorTheme),
                    errorBackground = LayerTheme(negativeColorTheme)
                ),
                uploading = DefaultUploadFileTheme(this),
                uploaded = DefaultUploadFileTheme(this),
                error = UploadFileTheme(text = BaseNegativeColorTextTheme(this), info = BaseDarkColorTextTheme(this)),
                progress = primaryColorTheme,
                errorProgress = negativeColorTheme,
                removeButton = normalColorTheme
            )
        )
    }
}

/**
 * Default theme for Disabled Input
 */
private fun ChatInputDisabledTheme(pallet: ColorPallet): InputTheme? = pallet.run {
    composeIfAtLeastOneNotNull(
        darkColorTheme,
        normalColorTheme,
        primaryColorTheme,
        shadeColorTheme,
        negativeColorTheme,
        neutralColorTheme
    ) {
        InputTheme(
            background = LayerTheme(fill = neutralColorTheme),
            text = BaseShaderColorTextTheme(this),
            placeholder = BaseShaderColorTextTheme(this),
            divider = shadeColorTheme,
            sendButton = ButtonTheme(iconColor = shadeColorTheme),
            mediaButton = ButtonTheme(iconColor = shadeColorTheme),
            fileUploadBar = FileUploadBarTheme(
                filePreview = FilePreviewTheme(
                    text = BaseLightColorTextTheme(this),
                    errorIcon = negativeColorTheme,
                    background = LayerTheme(neutralColorTheme),
                    errorBackground = LayerTheme(negativeColorTheme)
                ),
                uploading = DefaultUploadFileTheme(this),
                uploaded = DefaultUploadFileTheme(this),
                error = UploadFileTheme(text = BaseNegativeColorTextTheme(this), info = BaseDarkColorTextTheme(this)),
                progress = primaryColorTheme,
                errorProgress = negativeColorTheme,
                removeButton = normalColorTheme
            )
        )
    }
}

/**
 * Default theme for Unread messages indicator
 */
private fun ChatUnreadIndicatorTheme(pallet: ColorPallet): UnreadIndicatorTheme? = pallet.run {
    val bubble = BubbleTheme(this)
    composeIfAtLeastOneNotNull(bubble, lightColorTheme) {
        UnreadIndicatorTheme(
            background = lightColorTheme,
            bubble = bubble
        )
    }
}
