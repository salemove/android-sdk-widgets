package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeButton
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeResponseCardOption(
    val normal: ThemeButton?,
    val selected: ThemeButton?,
    val disabled: ThemeButton?,
) : Parcelable
