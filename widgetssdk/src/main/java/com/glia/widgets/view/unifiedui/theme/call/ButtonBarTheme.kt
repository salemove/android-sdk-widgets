package com.glia.widgets.view.unifiedui.theme.call

import com.glia.widgets.view.unifiedui.theme.base.BadgeTheme

internal data class ButtonBarTheme(
    val badge: BadgeTheme? = null,
    val chatButton: BarButtonStatesTheme? = null,
    val minimizeButton: BarButtonStatesTheme? = null,
    val muteButton: BarButtonStatesTheme? = null,
    val speakerButton: BarButtonStatesTheme? = null,
    val videoButton: BarButtonStatesTheme? = null
)
