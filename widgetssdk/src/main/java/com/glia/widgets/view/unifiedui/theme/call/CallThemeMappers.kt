package com.glia.widgets.view.unifiedui.theme.call

import com.glia.widgets.view.unifiedui.config.call.BarButtonStatesRemoteConfig
import com.glia.widgets.view.unifiedui.config.call.BarButtonStyleRemoteConfig
import com.glia.widgets.view.unifiedui.config.call.ButtonBarRemoteConfig
import com.glia.widgets.view.unifiedui.config.call.CallRemoteConfig
import com.glia.widgets.view.unifiedui.theme.base.updateFrom

internal fun ThemeBarButtonStyle?.updateFrom(barButtonStyleRemoteConfig: BarButtonStyleRemoteConfig?): ThemeBarButtonStyle? =
    barButtonStyleRemoteConfig?.let {
        ThemeBarButtonStyle(
            background = this?.background.updateFrom(it.background),
            imageColor = this?.imageColor.updateFrom(it.imageColor),
            title = this?.title.updateFrom(it.title)
        )
    } ?: this

internal fun ThemeBarButtonStates?.updateFrom(barButtonStatesRemoteConfig: BarButtonStatesRemoteConfig?): ThemeBarButtonStates? =
    barButtonStatesRemoteConfig?.let {
        ThemeBarButtonStates(
            inactive = this?.inactive.updateFrom(it.inactive),
            active = this?.active.updateFrom(it.active),
            selected = this?.selected.updateFrom(it.selected)
        )
    } ?: this

internal fun ThemeButtonBar?.updateFrom(buttonBarRemoteConfig: ButtonBarRemoteConfig?): ThemeButtonBar? = buttonBarRemoteConfig?.let {
    ThemeButtonBar(
        chatButton = this?.chatButton.updateFrom(it.chatButton),
        minimizeButton = this?.minimizeButton.updateFrom(it.minimizeButton),
        muteButton = this?.muteButton.updateFrom(it.muteButton),
        speakerButton = this?.speakerButton.updateFrom(it.speakerButton),
        videoButton = this?.videoButton.updateFrom(it.videoButton)
    )
} ?: this

internal fun CallTheme?.updateFrom(callRemoteConfig: CallRemoteConfig?): CallTheme? = callRemoteConfig?.let {
    CallTheme(
        background = this?.background.updateFrom(it.background),
        bottomText = this?.bottomText.updateFrom(it.bottomTextRemoteConfig),
        buttonBar = this?.buttonBar.updateFrom(it.buttonBarRemoteConfig),
        duration = this?.duration.updateFrom(it.duration),
        endButton = this?.endButton.updateFrom(it.endButtonRemoteConfig),
        header = this?.header.updateFrom(it.headerRemoteConfig),
        operator = this?.operator.updateFrom(it.operator),
        topText = this?.topText.updateFrom(it.topTextRemoteConfig)
    )
} ?: this