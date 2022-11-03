package com.glia.widgets.view.unifiedui.theme.base

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeText(
    val textColor: ThemeColor?,
    val backgroundColor: ThemeColor?,
    val textSize: Float?, // Size in SP
    val textStyle: Int?, //Typeface.NORMAL
    val textAlignment: Int? //TextView.TEXT_ALIGNMENT_TEXT_START
) : Parcelable
