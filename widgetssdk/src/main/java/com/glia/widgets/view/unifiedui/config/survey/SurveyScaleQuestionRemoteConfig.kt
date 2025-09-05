package com.glia.widgets.view.unifiedui.config.survey

import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.survey.SurveyScaleQuestionTheme
import com.google.gson.annotations.SerializedName

internal data class SurveyScaleQuestionRemoteConfig(

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("optionButton")
    val surveyOptionRemoteConfig: SurveyOptionRemoteConfig?
) {
    fun toSurveyScaleQuestionTheme() = SurveyScaleQuestionTheme(
        title = title?.toTextTheme(),
        surveyOption = surveyOptionRemoteConfig?.toSurveyOptionTheme()
    )
}
