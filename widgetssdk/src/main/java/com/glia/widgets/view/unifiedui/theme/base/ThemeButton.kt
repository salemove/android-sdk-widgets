package com.glia.widgets.view.unifiedui.theme.base

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeButton(
    val text: ThemeText?,
    val background: ThemeLayer?,
    val iconColor: ThemeColor?,
    val elevation: Float?,
    @ColorInt
    val shadowColor: Int?
) : Parcelable
