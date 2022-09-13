package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class InputTheme(
    val text: TextTheme?,
    val placeholder: TextTheme?,
    val divider: ColorTheme?,
    val sendButton: ButtonTheme?,
    val mediaButton: ButtonTheme?,
    val background: LayerTheme?,
    val fileUploadBar: FileUploadBarTheme?
)
