package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class InputTheme(
    val text: TextTheme? = null,
    val placeholder: TextTheme? = null,
    val divider: ColorTheme? = null,
    val sendButton: ButtonTheme? = null,
    val mediaButton: ButtonTheme? = null,
    val background: LayerTheme? = null,
    val fileUploadBar: FileUploadBarTheme? = null
) : Mergeable<InputTheme> {
    override fun merge(other: InputTheme): InputTheme = InputTheme(
        text = text merge other.text,
        placeholder = placeholder merge other.placeholder,
        divider = divider merge other.divider,
        sendButton = sendButton merge other.sendButton,
        mediaButton = mediaButton merge other.mediaButton,
        background = background merge other.background,
        fileUploadBar = fileUploadBar merge other.fileUploadBar
    )
}
