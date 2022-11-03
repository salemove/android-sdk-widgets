package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeButton
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import com.glia.widgets.view.unifiedui.theme.base.ThemeLayer
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeInput(
    val text: ThemeText?,
    val placeholder: ThemeText?,
    val divider: ThemeColor?,
    val sendButton: ThemeButton?,
    val mediaButton: ThemeButton?,
    val background: ThemeLayer?,
    val fileUploadBar: ThemeFileUploadBar?
) : Parcelable
