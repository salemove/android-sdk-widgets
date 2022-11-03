package com.glia.widgets.view.unifiedui.theme.base

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeHeader(
    val text: ThemeText?,
    val background: ThemeLayer?,
    val backButton: ThemeButton?,
    val closeButton: ThemeButton?,
    val endScreenSharingButton: ThemeButton?,
    val endButton: ThemeButton?
) : Parcelable
