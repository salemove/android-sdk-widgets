package com.glia.widgets.view.unifiedui.theme.alert

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class AlertTheme(
    val title: TextTheme? = null,
    val titleImageColor: ColorTheme? = null,
    val message: TextTheme? = null,
    val backgroundColor: ColorTheme? = null,
    val closeButtonColor: ColorTheme? = null,
    val linkButton: ButtonTheme? = null,
    val positiveButton: ButtonTheme? = null,
    val negativeButton: ButtonTheme? = null,
    val isVerticalAxis: Boolean? = null
) : Mergeable<AlertTheme> {
    override fun merge(other: AlertTheme): AlertTheme = AlertTheme(
        title = title merge other.title,
        titleImageColor = titleImageColor merge other.titleImageColor,
        message = message merge other.message,
        backgroundColor = backgroundColor merge other.backgroundColor,
        closeButtonColor = closeButtonColor merge other.closeButtonColor,
        linkButton = linkButton merge other.linkButton,
        positiveButton = positiveButton merge other.positiveButton,
        negativeButton = negativeButton merge other.negativeButton,
        isVerticalAxis = isVerticalAxis merge other.isVerticalAxis
    )
}
