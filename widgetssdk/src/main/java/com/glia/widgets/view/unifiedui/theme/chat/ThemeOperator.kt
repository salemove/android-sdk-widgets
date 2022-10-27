package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeOperator(
    val image: ThemeUserImage?,
    val animationColor: ThemeColor?,
    val overlayColor: ThemeColor?
) : Parcelable
