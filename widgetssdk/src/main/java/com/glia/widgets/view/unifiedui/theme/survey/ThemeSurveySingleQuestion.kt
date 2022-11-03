package com.glia.widgets.view.unifiedui.theme.survey

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeSurveySingleQuestion(
    val title: ThemeText?,
    val tintColor: ThemeColor?,
    val option: ThemeText?
) : Parcelable
