package com.glia.widgets.view.unifiedui.theme

import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme
import com.glia.widgets.view.unifiedui.theme.call.CallTheme
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.CallVisualizerTheme
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme
import com.glia.widgets.view.unifiedui.theme.secureconversations.SecureConversationsConfirmationScreenTheme
import com.glia.widgets.view.unifiedui.theme.secureconversations.SecureConversationsWelcomeScreenTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyTheme
import com.glia.widgets.view.unifiedui.webbrowser.WebBrowserTheme

internal data class UnifiedTheme(
    val alertTheme: AlertTheme? = null,
    val bubbleTheme: BubbleTheme? = null,
    val callTheme: CallTheme? = null,
    val chatTheme: ChatTheme? = null,
    val surveyTheme: SurveyTheme? = null,
    val callVisualizerTheme: CallVisualizerTheme? = null,
    val secureConversationsWelcomeScreenTheme: SecureConversationsWelcomeScreenTheme? = null,
    val secureConversationsConfirmationScreenTheme: SecureConversationsConfirmationScreenTheme? = null,
    val snackBarTheme: SnackBarTheme? = null,
    val webBrowserTheme: WebBrowserTheme? = null
)
