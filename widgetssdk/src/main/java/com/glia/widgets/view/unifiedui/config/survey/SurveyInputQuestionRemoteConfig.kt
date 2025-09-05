package com.glia.widgets.view.unifiedui.config.survey

import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.survey.SurveyInputQuestionTheme
import com.google.gson.annotations.SerializedName

internal data class SurveyInputQuestionRemoteConfig(

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("inputField")
    val inputField: SurveyOptionRemoteConfig?,

) {
    fun toSurveyInputQuestionTheme(): SurveyInputQuestionTheme = SurveyInputQuestionTheme(
        title = title?.toTextTheme(),
        inputField = inputField?.toSurveyOptionTheme()
    )
}
