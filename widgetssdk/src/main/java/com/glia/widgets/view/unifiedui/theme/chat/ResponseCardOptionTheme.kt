package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme

@JvmInline
internal value class ResponseCardOptionTheme(
    val normal: ButtonTheme? = null
//    No longer needed, because the Response card has only one state when it is open
//    val selected: ButtonTheme?,
//    val disabled: ButtonTheme?,
)
