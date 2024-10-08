package com.glia.widgets.view.unifiedui.theme.call

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.BadgeTheme

internal data class ButtonBarTheme(
    val badge: BadgeTheme? = null,
    val chatButton: BarButtonStatesTheme? = null,
    val minimizeButton: BarButtonStatesTheme? = null,
    val muteButton: BarButtonStatesTheme? = null,
    val speakerButton: BarButtonStatesTheme? = null,
    val videoButton: BarButtonStatesTheme? = null
) : Mergeable<ButtonBarTheme> {
    override fun merge(other: ButtonBarTheme): ButtonBarTheme = ButtonBarTheme(
        badge = badge merge other.badge,
        chatButton = chatButton merge other.chatButton,
        minimizeButton = minimizeButton merge other.minimizeButton,
        muteButton = muteButton merge other.muteButton,
        speakerButton = speakerButton merge other.speakerButton,
        videoButton = videoButton merge other.videoButton
    )
}
