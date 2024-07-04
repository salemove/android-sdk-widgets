package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class AttachmentItemTheme(
    val text: TextTheme? = null,
    val iconColor: ColorTheme? = null
) : Mergeable<AttachmentItemTheme> {
    override fun merge(other: AttachmentItemTheme): AttachmentItemTheme = AttachmentItemTheme(
        text = text merge other.text,
        iconColor = iconColor merge other.iconColor
    )
}
