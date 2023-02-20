package com.glia.widgets.view.unifiedui.config

import com.glia.widgets.view.unifiedui.config.alert.AlertRemoteConfig
import com.glia.widgets.view.unifiedui.config.bubble.BubbleRemoteConfig
import com.glia.widgets.view.unifiedui.config.call.CallRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.ChatRemoteConfig
import com.glia.widgets.view.unifiedui.config.endscreenshare.EndScreenShareRemoteConfig
import com.glia.widgets.view.unifiedui.config.survey.SurveyRemoteConfig
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.annotations.SerializedName

internal data class RemoteConfiguration(
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

    @SerializedName("endScreenSharing")
    val endScreenShareRemoteConfig: EndScreenShareRemoteConfig?
) {
    fun toUnifiedTheme(): UnifiedTheme = UnifiedTheme(
        alertTheme = alertRemoteConfig?.toAlertTheme(),
        bubbleTheme = bubbleRemoteConfig?.toBubbleTheme(),
        callTheme = callRemoteConfig?.toCallTheme(),
        chatTheme = chatRemoteConfig?.toChatTheme(),
        surveyTheme = surveyRemoteConfig?.toSurveyTheme(),
        endScreenSharingTheme = endScreenShareRemoteConfig?.toEndScreenShareTheme()
    )
}