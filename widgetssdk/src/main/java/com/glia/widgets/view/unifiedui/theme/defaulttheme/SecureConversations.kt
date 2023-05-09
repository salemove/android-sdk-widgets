@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import androidx.annotation.ColorInt
import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.*
import com.glia.widgets.view.unifiedui.theme.secureconversations.SecureConversationsConfirmationScreenTheme
import com.glia.widgets.view.unifiedui.theme.secureconversations.SecureConversationsWelcomeScreenTheme

internal fun SecureConversationsWelcomeScreenTheme(pallet: ColorPallet): SecureConversationsWelcomeScreenTheme =
    pallet.let {
        val baseDarkColorText = TextTheme(textColor = pallet.baseDarkColorTheme)

        SecureConversationsWelcomeScreenTheme(
            headerTheme = PrimaryColorHeaderTheme(it),
            welcomeTitleTheme = baseDarkColorText,
            titleImageTheme = it.primaryColorTheme,
            welcomeSubtitleTheme = baseDarkColorText,
            checkMessagesButtonTheme = BasePrimaryColorTextTheme(it),
            messageTitleTheme = baseDarkColorText,
            messageInputNormalTheme = DefaultInputTheme(
                stroke = it.baseShadeColorTheme?.primaryColor,
                textColor = it.baseDarkColorTheme
            ),
            messageInputActiveTheme = DefaultInputTheme(
                stroke = it.primaryColorTheme?.primaryColor,
                textColor = it.baseDarkColorTheme
            ),
            messageInputDisabledTheme = DefaultInputTheme(
                stroke = it.baseShadeColorTheme?.primaryColor,
                textColor = it.baseDarkColorTheme
            ),
            messageInputErrorTheme = DefaultInputTheme(
                stroke = it.systemNegativeColorTheme?.primaryColor,
                textColor = it.baseDarkColorTheme
            ),
            messageInputHintTheme = BaseNormalColorTextTheme(it),
            enabledSendButtonTheme = SecureConversationsEnabledSendButtonTheme(it),
            disabledSendButtonTheme = null,
            loadingSendButtonTheme = null,
            activityIndicatorColorTheme = null,
            messageWarningTheme = BaseNegativeColorTextTheme(it),
            messageWarningIconColorTheme = it.systemNegativeColorTheme,
            filePickerButtonTheme = it.baseNormalColorTheme,
            filePickerButtonDisabledTheme = it.baseShadeColorTheme,
            attachmentListTheme = null,
            pickMediaTheme = DefaultAttachmentsPopupTheme(it),
            backgroundTheme = it.backgroundColorTheme
        )
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
    fill: ColorTheme? = null, @ColorInt stroke: Int? = null, textColor: ColorTheme? = null
): TextInputTheme? = composeIfAtLeastOneNotNull(fill, stroke, textColor) {
    TextInputTheme(
        textTheme = TextTheme(textColor = textColor),
        backgroundTheme = LayerTheme(fill = fill, stroke = stroke)
    )
}

internal fun SecureConversationsConfirmationScreenTheme(pallet: ColorPallet): SecureConversationsConfirmationScreenTheme =
    pallet.let {
        SecureConversationsConfirmationScreenTheme(
            headerTheme = PrimaryColorHeaderTheme(it),
            backgroundTheme = it.backgroundColorTheme,
            iconColorTheme = it.primaryColorTheme,
            titleTheme = BaseDarkColorTextTheme(it),
            subtitleTheme = BaseDarkColorTextTheme(it),
            checkMessagesButtonTheme = PositiveDefaultButtonTheme(pallet)
        )
    }