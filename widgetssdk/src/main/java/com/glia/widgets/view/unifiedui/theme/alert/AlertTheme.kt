package com.glia.widgets.view.unifiedui.theme.alert

import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class AlertTheme(
    val title: TextTheme?,
    val titleImageColor: ColorTheme?,
    val message: TextTheme?,
    val backgroundColor: ColorTheme?,
    val closeButtonColor: ColorTheme?,
    val positiveButton: ButtonTheme?,
    val negativeButton: ButtonTheme?,
    val isVerticalAxis: Boolean?
)
