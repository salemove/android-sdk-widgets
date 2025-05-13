package com.glia.widgets.view.unifiedui.config

import com.glia.widgets.view.unifiedui.config.alert.AlertRemoteConfig
import com.glia.widgets.view.unifiedui.config.bubble.BubbleRemoteConfig
import com.glia.widgets.view.unifiedui.config.call.CallRemoteConfig
import com.glia.widgets.view.unifiedui.config.callvisualizer.CallVisualizerConfig
import com.glia.widgets.view.unifiedui.config.chat.ChatRemoteConfig
import com.glia.widgets.view.unifiedui.config.entrywidget.EntryWidgetRemoteConfig
import com.glia.widgets.view.unifiedui.config.securemessaging.SecureMessagingConfirmationScreenRemoteConfig
import com.glia.widgets.view.unifiedui.config.securemessaging.SecureMessagingWelcomeScreenRemoteConfig
import com.glia.widgets.view.unifiedui.config.snackbar.SnackBarRemoteConfig
import com.glia.widgets.view.unifiedui.config.survey.SurveyRemoteConfig
import com.glia.widgets.view.unifiedui.config.webbrowser.WebBrowserRemoteConfig
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.defaulttheme.DefaultTheme
import com.google.gson.annotations.SerializedName

internal data class RemoteConfiguration(
    @SerializedName("globalColors")
    val globalColorsConfig: GlobalColorsConfig?,

    @SerializedName("chatScreen")
    val chatRemoteConfig: ChatRemoteConfig?,

    @SerializedName("callScreen")
    val callRemoteConfig: CallRemoteConfig?,

    @SerializedName("surveyScreen")
    val surveyRemoteConfig: SurveyRemoteConfig?,

    @SerializedName("bubble")
    val bubbleRemoteConfig: BubbleRemoteConfig?,

    @SerializedName("alert")
    val alertRemoteConfig: AlertRemoteConfig?,

    @SerializedName("callVisualizer")
    val callVisualizerRemoteConfig: CallVisualizerConfig?,

    @SerializedName("secureMessagingWelcomeScreen")
    val secureMessagingWelcomeScreenRemoteConfig: SecureMessagingWelcomeScreenRemoteConfig?,

    @SerializedName("secureMessagingConfirmationScreen")
    val secureMessagingConfirmationScreenRemoteConfig: SecureMessagingConfirmationScreenRemoteConfig?,

    @SerializedName("snackBar")
    val snackBarRemoteConfig: SnackBarRemoteConfig?,

    @SerializedName("webBrowserScreen")
    val webBrowserRemoteConfig: WebBrowserRemoteConfig?,

    @SerializedName("entryWidget")
    val entryWidgetRemoteConfig: EntryWidgetRemoteConfig?,

    @SerializedName("isWhiteLabel")
    val isWhiteLabel: Boolean?
) {
    fun toUnifiedTheme(): UnifiedTheme? {
        val defaultTheme = DefaultTheme(globalColorsConfig?.toColorPallet())

        val unifiedTheme = UnifiedTheme(
            alertTheme = alertRemoteConfig?.toAlertTheme(),
            bubbleTheme = bubbleRemoteConfig?.toBubbleTheme(),
            callTheme = callRemoteConfig?.toCallTheme(),
            chatTheme = chatRemoteConfig?.toChatTheme(),
            surveyTheme = surveyRemoteConfig?.toSurveyTheme(),
            callVisualizerTheme = callVisualizerRemoteConfig?.toCallVisualizerTheme(),
            secureMessagingWelcomeScreenTheme = secureMessagingWelcomeScreenRemoteConfig?.toSecureMessagingWelcomeScreenTheme(),
            secureMessagingConfirmationScreenTheme = secureMessagingConfirmationScreenRemoteConfig
                ?.toSecureMessagingConfirmationScreenTheme(),
            snackBarTheme = snackBarRemoteConfig?.toSnackBarTheme(),
            webBrowserTheme = webBrowserRemoteConfig?.toWebBrowserTheme(),
            entryWidgetTheme = entryWidgetRemoteConfig?.toEntryWidgetTheme(),
            isWhiteLabel = isWhiteLabel
        )

        return defaultTheme merge unifiedTheme
    }
}
