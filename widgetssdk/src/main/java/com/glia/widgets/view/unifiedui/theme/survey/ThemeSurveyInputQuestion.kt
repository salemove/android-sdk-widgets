package com.glia.widgets.view.unifiedui.theme.survey

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeLayer
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeSurveyInputQuestion(
    val title: ThemeText?,
    val option: ThemeOptionButton?,
    val background: ThemeLayer?,
    val text: ThemeText?
) : Parcelable
