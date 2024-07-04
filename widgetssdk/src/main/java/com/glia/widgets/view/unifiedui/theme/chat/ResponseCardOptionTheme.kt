package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme

@JvmInline
internal value class ResponseCardOptionTheme(
    val normal: ButtonTheme? = null
) : Mergeable<ResponseCardOptionTheme> {
    override fun merge(other: ResponseCardOptionTheme): ResponseCardOptionTheme = ResponseCardOptionTheme(
        normal = normal merge other.normal
    )
}
