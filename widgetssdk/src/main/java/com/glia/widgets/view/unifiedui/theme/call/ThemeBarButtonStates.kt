package com.glia.widgets.view.unifiedui.theme.call

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeBarButtonStates(
    val inactive: ThemeBarButtonStyle?,
    val active: ThemeBarButtonStyle?,
    val selected: ThemeBarButtonStyle?
) : Parcelable
