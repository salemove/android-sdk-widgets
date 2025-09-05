package com.glia.widgets.view.unifiedui.theme.survey

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class SurveyBooleanQuestionTheme(
    val title: TextTheme? = null,
    val surveyOption: SurveyOptionTheme? = null
) : Mergeable<SurveyBooleanQuestionTheme> {
    override fun merge(other: SurveyBooleanQuestionTheme): SurveyBooleanQuestionTheme = SurveyBooleanQuestionTheme(
        title = title merge other.title,
        surveyOption = surveyOption merge other.surveyOption
    )
}
