package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class FilePreviewTheme(
    val text: TextTheme? = null,
    val errorIcon: ColorTheme? = null,
    val background: LayerTheme? = null,
    val errorBackground: LayerTheme? = null
) : Mergeable<FilePreviewTheme> {
    override fun merge(other: FilePreviewTheme): FilePreviewTheme = FilePreviewTheme(
        text = text merge other.text,
        errorIcon = errorIcon merge other.errorIcon,
        background = background merge other.background,
        errorBackground = errorBackground merge other.errorBackground
    )
}
