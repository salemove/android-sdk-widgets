package com.glia.widgets.view.unifiedui.theme.secureconversations

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class SecureConversationsConfirmationScreenTheme(
    val headerTheme: HeaderTheme? = null,
    val backgroundTheme: ColorTheme? = null,
    val iconColorTheme: ColorTheme? = null,
    val titleTheme: TextTheme? = null,
    val subtitleTheme: TextTheme? = null,
    val checkMessagesButtonTheme: ButtonTheme? = null
) : Mergeable<SecureConversationsConfirmationScreenTheme> {
    override fun merge(other: SecureConversationsConfirmationScreenTheme): SecureConversationsConfirmationScreenTheme =
        SecureConversationsConfirmationScreenTheme(
            headerTheme = headerTheme merge other.headerTheme,
            backgroundTheme = backgroundTheme merge other.backgroundTheme,
            iconColorTheme = iconColorTheme merge other.iconColorTheme,
            titleTheme = titleTheme merge other.titleTheme,
            subtitleTheme = subtitleTheme merge other.subtitleTheme,
            checkMessagesButtonTheme = checkMessagesButtonTheme merge other.checkMessagesButtonTheme
        )
}
