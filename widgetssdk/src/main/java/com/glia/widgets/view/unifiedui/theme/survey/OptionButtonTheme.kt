package com.glia.widgets.view.unifiedui.theme.survey

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
)
