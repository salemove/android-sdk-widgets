@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import androidx.annotation.ColorInt
import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextInputTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingConfirmationScreenTheme
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingTheme
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingWelcomeScreenTheme

internal fun SecureMessagingWelcomeScreenTheme(pallet: ColorPallet):
    SecureMessagingWelcomeScreenTheme {
    return pallet.let {
        val baseDarkColorText = TextTheme(textColor = pallet.darkColorTheme)

        SecureMessagingWelcomeScreenTheme(
            headerTheme = PrimaryColorHeaderTheme(it),
            welcomeTitleTheme = baseDarkColorText,
            titleImageTheme = it.primaryColorTheme,
            welcomeSubtitleTheme = baseDarkColorText,
            checkMessagesButtonTheme = BasePrimaryColorTextTheme(it),
            messageTitleTheme = baseDarkColorText,
            messageInputNormalTheme = DefaultInputTheme(
                stroke = it.shadeColorTheme?.primaryColor,
                textColor = it.darkColorTheme
            ),
            messageInputActiveTheme = DefaultInputTheme(
                stroke = it.primaryColorTheme?.primaryColor,
                textColor = it.darkColorTheme
            ),
            messageInputDisabledTheme = DefaultInputTheme(
                stroke = it.shadeColorTheme?.primaryColor,
                textColor = it.darkColorTheme
            ),
            messageInputErrorTheme = DefaultInputTheme(
                stroke = it.negativeColorTheme?.primaryColor,
                textColor = it.darkColorTheme
            ),
            messageInputHintTheme = BaseNormalColorTextTheme(it),
            enabledSendButtonTheme = SecureConversationsEnabledSendButtonTheme(it),
            disabledSendButtonTheme = null,
            loadingSendButtonTheme = null,
            activityIndicatorColorTheme = null,
            messageWarningTheme = BaseNegativeColorTextTheme(it),
            messageWarningIconColorTheme = it.negativeColorTheme,
            filePickerButtonTheme = it.normalColorTheme,
            filePickerButtonDisabledTheme = it.shadeColorTheme,
            attachmentListTheme = null,
            pickMediaTheme = DefaultAttachmentsPopupTheme(it),
            backgroundTheme = it.lightColorTheme
        )
    }
}

/**
 * Default theme for `Send message` button
 */
private fun SecureConversationsEnabledSendButtonTheme(pallet: ColorPallet): ButtonTheme? =
    PositiveDefaultButtonTheme(pallet)

/**
 * Default theme for TextInput
 */
private fun DefaultInputTheme(
    fill: ColorTheme? = null,
    @ColorInt stroke: Int? = null,
    textColor: ColorTheme? = null
): TextInputTheme? = composeIfAtLeastOneNotNull(fill, stroke, textColor) {
    TextInputTheme(
        textTheme = TextTheme(textColor = textColor),
        backgroundTheme = LayerTheme(fill = fill, stroke = stroke)
    )
}

internal fun SecureMessagingConfirmationScreenTheme(pallet: ColorPallet):
    SecureMessagingConfirmationScreenTheme {
    return pallet.let {
        SecureMessagingConfirmationScreenTheme(
            headerTheme = PrimaryColorHeaderTheme(it),
            backgroundTheme = it.lightColorTheme,
            iconColorTheme = it.primaryColorTheme,
            titleTheme = BaseDarkColorTextTheme(it),
            subtitleTheme = BaseDarkColorTextTheme(it),
            checkMessagesButtonTheme = PositiveDefaultButtonTheme(pallet)
        )
    }
}

internal fun SecureMessagingTheme(pallet: ColorPallet): SecureMessagingTheme {
    return pallet.let {
        SecureMessagingTheme(
            unavailableStatusBackground = LayerTheme(fill = it.negativeColorTheme),
            unavailableStatusText = BaseLightColorTextTheme(it),
            bottomBannerBackground = LayerTheme(fill = it.neutralColorTheme),
            bottomBannerText = BaseNormalColorTextTheme(it),
            bottomBannerDividerColor = it.shadeColorTheme,
            topBannerBackground = LayerTheme(fill = it.neutralColorTheme),
            topBannerText = BaseDarkColorTextTheme(it),
            topBannerDividerColor = it.shadeColorTheme,
            topBannerDropDownIconColor = it.normalColorTheme,
            mediaTypeItems = DefaultMediaTypeItemsTheme(it)
        )
    }
}

