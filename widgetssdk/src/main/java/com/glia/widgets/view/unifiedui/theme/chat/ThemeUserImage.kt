package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeUserImage(
    val placeholderColor: ThemeColor?,
    val placeholderBackgroundColor: ThemeColor?,
    val imageBackgroundColor: ThemeColor?,
) : Parcelable
