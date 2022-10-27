package com.glia.widgets.view.unifiedui.config.alert

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import com.google.gson.annotations.SerializedName

internal data class AlertRemoteConfig(

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("titleImageColor")
    val titleImageColor: ColorLayerRemoteConfig?,

    @SerializedName("message")
    val message: TextRemoteConfig?,

    @SerializedName("backgroundColor")
    val backgroundColor: ColorLayerRemoteConfig?,

    @SerializedName("closeButtonColor")
    val closeButtonColor: ColorLayerRemoteConfig?,

    @SerializedName("positiveButton")
    val positiveButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("negativeButton")
    val negativeButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("buttonAxis")
    val buttonAxisRemoteConfig: AxisRemoteConfig?
) {
    fun toAlertTheme(): AlertTheme = AlertTheme(
        title = title?.toTextTheme(),
        titleImageColor = titleImageColor?.toColorTheme(),
        message = message?.toTextTheme(),
        backgroundColor = backgroundColor?.toColorTheme(),
        closeButtonColor = closeButtonColor?.toColorTheme(),
        positiveButton = positiveButtonRemoteConfig?.toButtonTheme(),
        negativeButton = negativeButtonRemoteConfig?.toButtonTheme(),
        isVerticalAxis = buttonAxisRemoteConfig?.isVertical
    )
}
