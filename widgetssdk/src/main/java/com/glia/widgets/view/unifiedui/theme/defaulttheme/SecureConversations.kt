@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import androidx.annotation.ColorInt
import com.glia.widgets.view.unifiedui.extensions.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.*
import com.glia.widgets.view.unifiedui.theme.secureconversations.SecureConversationsWelcomeScreenTheme

internal fun SecureConversationsWelcomeScreenTheme(pallet: ColorPallet): SecureConversationsWelcomeScreenTheme =
    pallet.let {
        val baseDarkColorText = TextTheme(textColor = pallet.baseDarkColorTheme)

        SecureConversationsWelcomeScreenTheme(
            headerTheme = PrimaryColorHeaderTheme(it),
            welcomeTitleTheme = baseDarkColorText,
            titleImageTheme = it.primaryColorTheme,
            welcomeSubtitleTheme = baseDarkColorText,
            checkMessagesButtonTheme = TextTheme(textColor = it.primaryColorTheme),
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
            messageInputHintTheme = TextTheme(textColor = it.baseNormalColorTheme),
            enabledSendButtonTheme = SecureConversationsEnabledSendButtonTheme(it),
            disabledSendButtonTheme = null,
            loadingSendButtonTheme = null,
            activityIndicatorColorTheme = null,
            messageWarningTheme = TextTheme(textColor = it.systemNegativeColorTheme),
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
    pallet.run {
        composeIfAtLeastOneNotNull(baseLightColorTheme, primaryColorTheme) {
            ButtonTheme(
                text = TextTheme(textColor = baseLightColorTheme),
                background = LayerTheme(
                    fill = primaryColorTheme,
                    stroke = primaryColorTheme?.primaryColor
                )
            )
        }
    }

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