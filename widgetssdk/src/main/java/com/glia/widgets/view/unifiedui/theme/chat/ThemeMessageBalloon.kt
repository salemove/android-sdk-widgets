package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeLayer
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeMessageBalloon(
    val background: ThemeLayer?,
    val text: ThemeText?,
    val status: ThemeText?,
    val alignment: Int?
) : Parcelable
