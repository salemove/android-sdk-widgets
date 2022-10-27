package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeEngagementStates(
    val operator: ThemeOperator?,
    val queue: ThemeEngagementState?,
    val connecting: ThemeEngagementState?,
    val connected: ThemeEngagementState?,
    val transferring: ThemeEngagementState?,
    val onHold: ThemeEngagementState?
) : Parcelable
