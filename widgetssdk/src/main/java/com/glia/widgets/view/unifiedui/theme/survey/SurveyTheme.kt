package com.glia.widgets.view.unifiedui.theme.survey

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class SurveyTheme(
    val layer: LayerTheme? = null,
    val title: TextTheme? = null,
    val submitButton: ButtonTheme? = null,
    val cancelButton: ButtonTheme? = null,
    val booleanQuestion: SurveyBooleanQuestionTheme? = null,
    val scaleQuestion: SurveyScaleQuestionTheme? = null,
    val singleQuestion: SurveySingleQuestionTheme? = null,
    val inputQuestion: SurveyInputQuestionTheme? = null
) : Mergeable<SurveyTheme> {
    override fun merge(other: SurveyTheme): SurveyTheme = SurveyTheme(
        layer = layer merge other.layer,
        title = title merge other.title,
        submitButton = submitButton merge other.submitButton,
        cancelButton = cancelButton merge other.cancelButton,
        booleanQuestion = booleanQuestion merge other.booleanQuestion,
        scaleQuestion = scaleQuestion merge other.scaleQuestion,
        singleQuestion = singleQuestion merge other.singleQuestion,
        inputQuestion = inputQuestion merge other.inputQuestion
    )
}
