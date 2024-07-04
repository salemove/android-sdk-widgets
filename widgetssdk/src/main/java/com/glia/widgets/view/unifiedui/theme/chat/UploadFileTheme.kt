package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class UploadFileTheme(
    val text: TextTheme?,
    val info: TextTheme?
) : Mergeable<UploadFileTheme> {
    override fun merge(other: UploadFileTheme): UploadFileTheme = UploadFileTheme(
        text = text merge other.text,
        info = info merge other.info
    )
}
