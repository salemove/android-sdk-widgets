package com.glia.widgets.view.unifiedui.theme.survey

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class OptionButtonTheme(
    val normalText: TextTheme? = null,
    val normalLayer: LayerTheme? = null,
    val selectedText: TextTheme? = null,
    val selectedLayer: LayerTheme? = null,
    val highlightedText: TextTheme? = null,
    val highlightedLayer: LayerTheme? = null,
    val fontSize: Float? = null,
    val fontStyle: Int? = null
) : Mergeable<OptionButtonTheme> {
    override fun merge(other: OptionButtonTheme): OptionButtonTheme = OptionButtonTheme(
        normalText = normalText merge other.normalText,
        normalLayer = normalLayer merge other.normalLayer,
        selectedText = selectedText merge other.selectedText,
        selectedLayer = selectedLayer merge other.selectedLayer,
        highlightedText = highlightedText merge other.highlightedText,
        highlightedLayer = highlightedLayer merge other.highlightedLayer,
        fontSize = fontSize merge other.fontSize,
        fontStyle = fontStyle merge other.fontStyle
    )
}
