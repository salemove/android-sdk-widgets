package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeEngagementState(
    val title: ThemeText?,
    val description: ThemeText?,
    val tintColor: ThemeColor?,
) : Parcelable
