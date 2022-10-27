package com.glia.widgets.view.unifiedui.config.survey

import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.survey.SurveyBooleanQuestionTheme
import com.google.gson.annotations.SerializedName

internal data class SurveyBooleanQuestionRemoteConfig(

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("optionButton")
    val optionButtonRemoteConfig: OptionButtonRemoteConfig?
) {
    fun toSurveyBooleanQuestionTheme(): SurveyBooleanQuestionTheme = SurveyBooleanQuestionTheme(
        title = title?.toTextTheme(),
        optionButton = optionButtonRemoteConfig?.toOptionButtonTheme()
    )
}