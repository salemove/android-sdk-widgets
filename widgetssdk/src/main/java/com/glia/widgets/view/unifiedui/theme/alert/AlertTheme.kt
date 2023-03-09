package com.glia.widgets.view.unifiedui.theme.alert

import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class AlertTheme(
    val title: TextTheme? = null,
    val titleImageColor: ColorTheme? = null,
    val message: TextTheme? = null,
    val backgroundColor: ColorTheme? = null,
    val closeButtonColor: ColorTheme? = null,
    val positiveButton: ButtonTheme? = null,
    val negativeButton: ButtonTheme? = null,
    val isVerticalAxis: Boolean? = null
)
