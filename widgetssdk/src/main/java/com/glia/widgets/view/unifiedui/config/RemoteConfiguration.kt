package com.glia.widgets.view.unifiedui.config

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.alert.AlertRemoteConfig
import com.glia.widgets.view.unifiedui.config.bubble.BubbleRemoteConfig
import com.glia.widgets.view.unifiedui.config.call.CallRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.ChatRemoteConfig
import com.glia.widgets.view.unifiedui.config.survey.SurveyRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class RemoteConfiguration(
    @SerializedName("chatScreen")
    val chatRemoteConfig: ChatRemoteConfig?,

    @SerializedName("callScreen")
    val callRemoteConfig: CallRemoteConfig?,

    @SerializedName("surveyScreen")
    val surveyRemoteConfig: SurveyRemoteConfig,

    @SerializedName("bubble")
    val bubble: BubbleRemoteConfig?,

    @SerializedName("alert")
    val alertRemoteConfig: AlertRemoteConfig?
) : Parcelable