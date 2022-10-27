package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeLayer
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeResponseCard(
    val background: ThemeLayer?,
    val option: ThemeResponseCardOption?,
    val message: ThemeMessageBalloon?,
) : Parcelable
