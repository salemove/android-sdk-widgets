package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class MessageBalloonTheme(
    val background: LayerTheme? = null,
    val text: TextTheme? = null,
    val status: TextTheme? = null,
    val error: TextTheme? = null,
    val userImage: UserImageTheme? = null
) : Mergeable<MessageBalloonTheme> {
    override fun merge(other: MessageBalloonTheme): MessageBalloonTheme = MessageBalloonTheme(
        background = background merge other.background,
        text = text merge other.text,
        status = status merge other.status,
        error = error merge other.error,
        userImage = userImage merge other.userImage
    )
}
