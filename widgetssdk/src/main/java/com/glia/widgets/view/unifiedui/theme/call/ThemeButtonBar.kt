package com.glia.widgets.view.unifiedui.theme.call

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThemeButtonBar(
    val chatButton: ThemeBarButtonStates?,
    val minimizeButton: ThemeBarButtonStates?,
    val muteButton: ThemeBarButtonStates?,
    val speakerButton: ThemeBarButtonStates?,
    val videoButton: ThemeBarButtonStates?
) : Parcelable
