package com.glia.widgets.view.unifiedui.theme.survey

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class SurveySingleQuestionTheme(
    val title: TextTheme? = null,
    val tintColor: ColorTheme? = null,
    val option: TextTheme? = null,
    val error: TextTheme? = null
) : Mergeable<SurveySingleQuestionTheme> {
    override fun merge(other: SurveySingleQuestionTheme): SurveySingleQuestionTheme = SurveySingleQuestionTheme(
        title = title merge other.title,
        tintColor = tintColor merge other.tintColor,
        option = option merge other.option,
        error = error merge other.error
    )
}
