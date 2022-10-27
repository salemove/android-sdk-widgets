package com.glia.widgets.view.unifiedui.theme.survey

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeSurveyBooleanQuestion(
    val title: ThemeText?,
    val optionButton: ThemeOptionButton?
) : Parcelable
