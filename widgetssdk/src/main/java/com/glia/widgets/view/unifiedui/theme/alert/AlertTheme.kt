package com.glia.widgets.view.unifiedui.theme.alert

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.alert.AlertRemoteConfig
import com.glia.widgets.view.unifiedui.theme.base.ThemeButton
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import com.glia.widgets.view.unifiedui.theme.base.updateFrom
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AlertTheme(
    val title: ThemeText?,
    val titleImageColor: ThemeColor?,
    val message: ThemeText?,
    val backgroundColor: ThemeColor?,
    val closeButtonColor: ThemeColor?,
    val positiveButton: ThemeButton?,
    val negativeButton: ThemeButton?,
    val isVerticalAxis: Boolean?
) : Parcelable

internal fun AlertTheme?.updateFrom(alertRemoteConfig: AlertRemoteConfig?): AlertTheme? = alertRemoteConfig?.let {
    AlertTheme(
        title = this?.title.updateFrom(it.title),
        titleImageColor = this?.titleImageColor.updateFrom(it.titleImageColor),
        message = this?.message.updateFrom(it.message),
        backgroundColor = this?.backgroundColor.updateFrom(it.backgroundColor),
        closeButtonColor = this?.closeButtonColor.updateFrom(it.closeButtonColor),
        positiveButton = this?.positiveButton.updateFrom(it.positiveButtonRemoteConfig),
        negativeButton = this?.negativeButton.updateFrom(it.negativeButtonRemoteConfig),
        isVerticalAxis = it.buttonAxisRemoteConfig?.isVertical ?: this?.isVerticalAxis
    )
} ?: this
