package com.glia.widgets.view.unifiedui.theme.securemessaging

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class SecureMessagingConfirmationScreenTheme(
    val headerTheme: HeaderTheme? = null,
    val backgroundTheme: ColorTheme? = null,
    val iconColorTheme: ColorTheme? = null,
    val titleTheme: TextTheme? = null,
    val subtitleTheme: TextTheme? = null,
    val checkMessagesButtonTheme: ButtonTheme? = null
) : Mergeable<SecureMessagingConfirmationScreenTheme> {
    override fun merge(other: SecureMessagingConfirmationScreenTheme): SecureMessagingConfirmationScreenTheme =
        SecureMessagingConfirmationScreenTheme(
            headerTheme = headerTheme merge other.headerTheme,
            backgroundTheme = backgroundTheme merge other.backgroundTheme,
            iconColorTheme = iconColorTheme merge other.iconColorTheme,
            titleTheme = titleTheme merge other.titleTheme,
            subtitleTheme = subtitleTheme merge other.subtitleTheme,
            checkMessagesButtonTheme = checkMessagesButtonTheme merge other.checkMessagesButtonTheme
        )
}
