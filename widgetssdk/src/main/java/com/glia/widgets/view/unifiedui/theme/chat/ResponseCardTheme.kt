package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class ResponseCardTheme(
    val background: LayerTheme? = null,
    val option: ResponseCardOptionTheme? = null,
    val text: TextTheme? = null,
    val userImage: UserImageTheme? = null
) : Mergeable<ResponseCardTheme> {
    override fun merge(other: ResponseCardTheme): ResponseCardTheme = ResponseCardTheme(
        background = background merge other.background,
        option = option merge other.option,
        text = text merge other.text,
        userImage = userImage merge other.userImage
    )
}
