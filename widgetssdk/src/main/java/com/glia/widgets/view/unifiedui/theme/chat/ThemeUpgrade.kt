package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import com.glia.widgets.view.unifiedui.theme.base.ThemeLayer
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeUpgrade(
    val text: ThemeText?,
    val description: ThemeText?,
    val iconColor: ThemeColor?,
    val background: ThemeLayer?,
) : Parcelable
