package com.glia.widgets.view.unifiedui.theme.bubble

import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.chat.UserImageTheme

internal data class BubbleTheme(
    val userImage: UserImageTheme?,
    val badge: ButtonTheme?,
    val onHoldOverlay: ColorTheme?
)