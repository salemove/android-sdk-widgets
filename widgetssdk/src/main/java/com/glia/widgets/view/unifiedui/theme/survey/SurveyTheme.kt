package com.glia.widgets.view.unifiedui.theme.survey

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeButton
import com.glia.widgets.view.unifiedui.theme.base.ThemeLayer
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SurveyTheme(
    val layer: ThemeLayer?,
    val title: ThemeText?,
    val submitButton: ThemeButton?,
    val cancelButton: ThemeButton?,
    val booleanQuestion: ThemeSurveyBooleanQuestion?,
    val scaleQuestion: ThemeSurveyScaleQuestion?,
    val singleQuestion: ThemeSurveySingleQuestion?,
    val inputQuestion: ThemeSurveyInputQuestion?
) : Parcelable
