package com.glia.widgets.view.unifiedui.theme.survey

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
)
