package com.glia.widgets.view.unifiedui.theme.base

import android.content.res.ColorStateList
import android.graphics.Typeface
import com.glia.widgets.view.configuration.ButtonConfiguration
import com.glia.widgets.view.configuration.LayerConfiguration
import com.glia.widgets.view.configuration.TextConfiguration
import com.glia.widgets.view.unifiedui.config.base.*
import com.glia.widgets.view.unifiedui.config.chat.BadgeRemoteConfig
import com.glia.widgets.view.unifiedui.parse.parseColorOrNull


internal fun ThemeColor?.updateFrom(colorLayerRemoteConfig: ColorLayerRemoteConfig?): ThemeColor? =
    colorLayerRemoteConfig?.let {
        ThemeColor(isGradient = it.isGradient, values = it.valuesExpanded)
    } ?: this

internal fun ThemeColor?.updateFrom(colorHex: String?): ThemeColor? =
    parseColorOrNull(colorHex)?.let { ThemeColor(values = listOf(it)) } ?: this

internal fun ThemeColor?.updateFrom(colorStateList: ColorStateList?): ThemeColor? =
    colorStateList?.let {
        ThemeColor(values = listOf(it.defaultColor))
    } ?: this

internal fun ThemeLayer?.updateFrom(layerRemoteConfig: LayerRemoteConfig?): ThemeLayer? =
    layerRemoteConfig?.let {
        ThemeLayer(
            fill = this?.fill.updateFrom(it.color),
            stroke = it.borderColor?.primaryColor ?: this?.stroke,
            borderWidth = it.borderWidth?.valuePx ?: this?.borderWidth,
            cornerRadius = it.cornerRadius?.valuePx ?: this?.cornerRadius
        )
    } ?: this

internal fun ThemeText?.updateFrom(textRemoteConfig: TextRemoteConfig?): ThemeText? =
    textRemoteConfig?.let {
        ThemeText(
            textColor = this?.textColor.updateFrom(it.textColor),
            backgroundColor = this?.backgroundColor.updateFrom(it.backgroundColor),
            textSize = it.fontSize ?: this?.textSize,
            textStyle = it.fontStyle ?: this?.textStyle,
            textAlignment = it.nativeAlignment ?: this?.textAlignment
        )
    } ?: this

internal fun ThemeText?.updateFrom(badgeRemoteConfig: BadgeRemoteConfig?): ThemeText? =
    badgeRemoteConfig?.let {
        ThemeText(
            textColor = this?.textColor.updateFrom(it.fontColor),
            backgroundColor = this?.backgroundColor.updateFrom(it.backgroundColor),
            textSize = it.fontSize ?: this?.textSize,
            textStyle = it.fontStyle ?: this?.textStyle,
            textAlignment = this?.textAlignment
        )
    } ?: this

internal fun ThemeButton?.updateFrom(buttonRemoteConfig: ButtonRemoteConfig?) =
    buttonRemoteConfig?.let {
        ThemeButton(
            text = this?.text.updateFrom(it.textRemoteConfig),
            background = this?.background.updateFrom(it.background),
            iconColor = this?.iconColor.updateFrom(buttonRemoteConfig.tintColor),
            elevation = it.elevation ?: this?.elevation,
            shadowColor = it.shadowColor ?: this?.shadowColor
        )
    } ?: this

internal fun ThemeHeader?.updateFrom(headerRemoteConfig: HeaderRemoteConfig?) =
    headerRemoteConfig?.let {
        ThemeHeader(
            text = this?.text.updateFrom(it.textRemoteConfig),
            background = this?.background.updateFrom(it.background),
            backButton = this?.backButton.updateFrom(it.backButtonRemoteConfig),
            closeButton = this?.closeButton.updateFrom(it.closeButtonRemoteConfig),
            endScreenSharingButton = this?.endScreenSharingButton.updateFrom(it.endScreenSharingButtonRemoteConfig),
            endButton = this?.endButton.updateFrom(it.endButtonRemoteConfig),
        )
    } ?: this

//--------------------------------------------------------------------------------------------------
//map from Runtime Config classes
@Deprecated("this a part of deprecated UiTheme")
internal fun ThemeLayer?.updateFrom(layerConfig: LayerConfiguration?): ThemeLayer? =
    layerConfig?.let {
        ThemeLayer(
            fill = this?.fill.updateFrom(it.backgroundColor),
            stroke = parseColorOrNull(it.borderColor) ?: this?.stroke,
            borderWidth = it.borderWidth.toFloat(),
            cornerRadius = it.cornerRadius.toFloat()
        )
    } ?: this

@Deprecated("this a part of deprecated UiTheme")
internal fun ThemeText?.updateFrom(textConfiguration: TextConfiguration?): ThemeText? =
    textConfiguration?.let {
        ThemeText(
            textColor = this?.textColor.updateFrom(it.textColor),
            backgroundColor = this?.backgroundColor,
            textSize = it.textSize,
            textStyle = if (it.isBold) Typeface.BOLD else this?.textStyle,
            textAlignment = this?.textAlignment
        )
    } ?: this

@Deprecated("this a part of deprecated UiTheme")
internal fun ThemeButton?.updateFrom(buttonConfiguration: ButtonConfiguration?): ThemeButton? =
    buttonConfiguration?.let {
        ThemeButton(
            text = this?.text.updateFrom(it.textConfiguration),
            background = it.backgroundColor?.run {  ThemeLayer(fill = ThemeColor(values = listOf(defaultColor))) } ?: this?.background,
            iconColor = this?.iconColor,
            elevation = this?.elevation,
            shadowColor = this?.shadowColor
        )
    } ?: this