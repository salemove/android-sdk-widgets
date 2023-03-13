package com.glia.widgets.view.unifiedui.config.survey

import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.survey.SurveyInputQuestionTheme
import com.google.gson.annotations.SerializedName

internal data class SurveyInputQuestionRemoteConfig(

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("option")
    val option: OptionButtonRemoteConfig?,

    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?
) {
    fun toSurveyInputQuestionTheme(): SurveyInputQuestionTheme = SurveyInputQuestionTheme(
        title = title?.toTextTheme(),
        text = textRemoteConfig?.toTextTheme(),
        option = option?.toOptionButtonTheme()
    )
}
