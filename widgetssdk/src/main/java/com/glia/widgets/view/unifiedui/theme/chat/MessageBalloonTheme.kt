package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class MessageBalloonTheme(
    val background: LayerTheme? = null,
    val text: TextTheme? = null,
    val status: TextTheme? = null,
    val userImage: UserImageTheme? = null
)
