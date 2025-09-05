package com.glia.widgets.view.unifiedui.theme.survey

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class SurveyScaleQuestionTheme(
    val title: TextTheme? = null,
    val surveyOption: SurveyOptionTheme? = null
) : Mergeable<SurveyScaleQuestionTheme> {
    override fun merge(other: SurveyScaleQuestionTheme): SurveyScaleQuestionTheme = SurveyScaleQuestionTheme(
        title = title merge other.title,
        surveyOption = surveyOption merge other.surveyOption
    )
}
