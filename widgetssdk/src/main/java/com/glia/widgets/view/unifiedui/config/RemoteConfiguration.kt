package com.glia.widgets.view.unifiedui.config

import com.glia.widgets.view.unifiedui.config.alert.AlertRemoteConfig
import com.glia.widgets.view.unifiedui.config.bubble.BubbleRemoteConfig
import com.glia.widgets.view.unifiedui.config.call.CallRemoteConfig
import com.glia.widgets.view.unifiedui.config.callvisualizer.CallVisualizerConfig
import com.glia.widgets.view.unifiedui.config.chat.ChatRemoteConfig
import com.glia.widgets.view.unifiedui.config.secureconversations.SecureConversationsConfirmationScreenRemoteConfig
import com.glia.widgets.view.unifiedui.config.secureconversations.SecureConversationsWelcomeScreenRemoteConfig
import com.glia.widgets.view.unifiedui.config.survey.SurveyRemoteConfig
import com.glia.widgets.view.unifiedui.extensions.deepMerge
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

    @SerializedName("secureConversationsWelcomeScreen")
    val secureConversationsWelcomeScreenRemoteConfig: SecureConversationsWelcomeScreenRemoteConfig?,

    @SerializedName("secureConversationsConfirmationScreen")
    val secureConversationsConfirmationScreenRemoteConfig: SecureConversationsConfirmationScreenRemoteConfig?
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
            secureConversationsWelcomeScreenTheme = secureConversationsWelcomeScreenRemoteConfig?.toSecureConversationsWelcomeScreenTheme(),
            secureConversationsConfirmationScreenTheme = secureConversationsConfirmationScreenRemoteConfig?.toSecureConversationsConfirmationScreenTheme()
        )

        return defaultTheme deepMerge unifiedTheme
    }
}
