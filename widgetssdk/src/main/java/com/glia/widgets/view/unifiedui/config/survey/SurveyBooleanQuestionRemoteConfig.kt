package com.glia.widgets.view.unifiedui.config.survey

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SurveyBooleanQuestionRemoteConfig(

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("optionButton")
    val optionButtonRemoteConfig: OptionButtonRemoteConfig?
): Parcelable