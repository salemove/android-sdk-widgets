package com.glia.widgets.view.unifiedui.theme.survey

import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class SurveyTheme(
    val layer: LayerTheme?,
    val title: TextTheme?,
    val submitButton: ButtonTheme?,
    val cancelButton: ButtonTheme?,
    val booleanQuestion: SurveyBooleanQuestionTheme?,
    val scaleQuestion: SurveyScaleQuestionTheme?,
    val singleQuestion: SurveySingleQuestionTheme?,
    val inputQuestion: SurveyInputQuestionTheme?
)
