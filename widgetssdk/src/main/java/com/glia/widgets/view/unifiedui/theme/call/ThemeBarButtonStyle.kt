package com.glia.widgets.view.unifiedui.theme.call

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeBarButtonStyle(
    val background: ThemeColor?,
    val imageColor: ThemeColor?,
    val title: ThemeText?
) : Parcelable
