package com.glia.widgets.view.unifiedui.theme.survey

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeLayer
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeOptionButton(
    val normalText: ThemeText?,
    val normalLayer: ThemeLayer?,
    val selectedText: ThemeText?,
    val selectedLayer: ThemeLayer?,
    val highlightedText: ThemeText?,
    val highlightedLayer: ThemeLayer?,
    val fontSize: Float?,
    val fontStyle: Int?
) : Parcelable
