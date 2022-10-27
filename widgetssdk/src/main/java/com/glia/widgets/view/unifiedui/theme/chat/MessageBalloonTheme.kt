package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class MessageBalloonTheme(
    val background: LayerTheme?,
    val text: TextTheme?,
    val status: TextTheme?,
    val alignment: Int?,
    val userImage: UserImageTheme?
)
