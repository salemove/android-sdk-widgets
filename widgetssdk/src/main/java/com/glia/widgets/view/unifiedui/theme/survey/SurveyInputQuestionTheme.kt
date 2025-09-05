package com.glia.widgets.view.unifiedui.theme.survey

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class SurveyInputQuestionTheme(
    val title: TextTheme? = null,
    val inputField: SurveyOptionTheme? = null,
) : Mergeable<SurveyInputQuestionTheme> {
    override fun merge(other: SurveyInputQuestionTheme): SurveyInputQuestionTheme = SurveyInputQuestionTheme(
        title = title merge other.title,
        inputField = inputField merge other.inputField
    )
}
