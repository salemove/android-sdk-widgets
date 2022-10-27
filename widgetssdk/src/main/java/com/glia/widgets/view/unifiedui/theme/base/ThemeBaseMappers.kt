package com.glia.widgets.view.unifiedui.theme.base

import com.glia.widgets.view.unifiedui.config.base.*
import com.glia.widgets.view.unifiedui.config.chat.BadgeRemoteConfig


internal fun ThemeColor?.updateFrom(colorLayerRemoteConfig: ColorLayerRemoteConfig?): ThemeColor? = colorLayerRemoteConfig?.let {
    ThemeColor(isGradient = it.isGradient, values = it.valuesExpanded)
} ?: this

internal fun ThemeLayer?.updateFrom(layerRemoteConfig: LayerRemoteConfig?): ThemeLayer? = layerRemoteConfig?.let {
    ThemeLayer(
        fill = this?.fill.updateFrom(it.color),
        stroke = it.borderColor?.primaryColor ?: this?.stroke,
        borderWidth = it.borderWidth?.valuePx ?: this?.borderWidth,
        cornerRadius = it.cornerRadius?.valuePx ?: this?.cornerRadius
    )
} ?: this

internal fun ThemeText?.updateFrom(textRemoteConfig: TextRemoteConfig?): ThemeText? = textRemoteConfig?.let {
    ThemeText(
        textColor = this?.textColor.updateFrom(it.textColor),
        backgroundColor = this?.backgroundColor.updateFrom(it.backgroundColor),
        textSize = it.fontSize ?: this?.textSize,
        textStyle = it.fontStyle ?: this?.textStyle,
        textAlignment = it.nativeAlignment ?: this?.textAlignment
    )
} ?: this

internal fun ThemeText?.updateFrom(badgeRemoteConfig: BadgeRemoteConfig?): ThemeText? = badgeRemoteConfig?.let {
    ThemeText(
        textColor = this?.textColor.updateFrom(it.fontColor),
        backgroundColor = this?.backgroundColor.updateFrom(it.backgroundColor),
        textSize = it.fontSize ?: this?.textSize,
        textStyle = it.fontStyle ?: this?.textStyle,
        textAlignment = this?.textAlignment
    )
} ?: this

internal fun ThemeButton?.updateFrom(buttonRemoteConfig: ButtonRemoteConfig?) = buttonRemoteConfig?.let {
    ThemeButton(
        text = this?.text.updateFrom(it.textRemoteConfig),
        background = this?.background.updateFrom(it.background),
        iconColor = this?.iconColor.updateFrom(buttonRemoteConfig.tintColor),
        elevation = it.elevation ?: this?.elevation,
        shadowColor = it.shadowColor ?: this?.shadowColor
    )
} ?: this

internal fun ThemeHeader?.updateFrom(headerRemoteConfig: HeaderRemoteConfig?) = headerRemoteConfig?.let {
    ThemeHeader(
        text = this?.text.updateFrom(it.textRemoteConfig),
        background = this?.background.updateFrom(it.background),
        backButton = this?.backButton.updateFrom(it.backButtonRemoteConfig),
        closeButton = this?.closeButton.updateFrom(it.closeButtonRemoteConfig),
        endScreenSharingButton = this?.endScreenSharingButton.updateFrom(it.endScreenSharingButtonRemoteConfig),
        endButton = this?.endButton.updateFrom(it.endButtonRemoteConfig),
    )
} ?: this