package com.glia.widgets.view.unifiedui.config.survey

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SurveyInputQuestionRemoteConfig(

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("option")
    val option: OptionButtonRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?
) : Parcelable
