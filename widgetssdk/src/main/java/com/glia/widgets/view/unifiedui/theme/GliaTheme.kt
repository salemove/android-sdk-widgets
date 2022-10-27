package com.glia.widgets.view.unifiedui.theme

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.RemoteConfiguration
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import com.glia.widgets.view.unifiedui.theme.alert.updateFrom
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme
import com.glia.widgets.view.unifiedui.theme.bubble.updateFrom
import com.glia.widgets.view.unifiedui.theme.call.CallTheme
import com.glia.widgets.view.unifiedui.theme.call.updateFrom
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme
import com.glia.widgets.view.unifiedui.theme.chat.updateFrom
import com.glia.widgets.view.unifiedui.theme.survey.SurveyTheme
import com.glia.widgets.view.unifiedui.theme.survey.updateFrom
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class GliaTheme(
    val alertTheme: AlertTheme?,
    val bubbleTheme: BubbleTheme?,
    val callTheme: CallTheme?,
    val chatTheme: ChatTheme?,
    val surveyTheme: SurveyTheme?
) : Parcelable {

    fun updateFrom(remoteConfiguration: RemoteConfiguration?): GliaTheme =
        remoteConfiguration?.let {
            copy(
                alertTheme = alertTheme.updateFrom(it.alertRemoteConfig),
                bubbleTheme = bubbleTheme.updateFrom(it.bubble),
                callTheme = callTheme.updateFrom(it.callRemoteConfig),
                chatTheme = chatTheme.updateFrom(it.chatRemoteConfig),
                surveyTheme = surveyTheme.updateFrom(it.surveyRemoteConfig)
            )
        } ?: this

}