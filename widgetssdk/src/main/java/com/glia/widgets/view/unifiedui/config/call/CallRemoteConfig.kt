package com.glia.widgets.view.unifiedui.config.call

import com.glia.widgets.view.unifiedui.config.base.HeaderRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.EngagementStatesRemoteConfig
import com.glia.widgets.view.unifiedui.config.snackbar.SnackBarRemoteConfig
import com.glia.widgets.view.unifiedui.theme.call.CallTheme
import com.google.gson.annotations.SerializedName

internal data class CallRemoteConfig(
    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("bottomText")
    val bottomTextRemoteConfig: TextRemoteConfig?,

    @SerializedName("buttonBar")
    val buttonBarRemoteConfig: ButtonBarRemoteConfig?,

    @SerializedName("duration")
    val duration: TextRemoteConfig?,

    @SerializedName("header")
    val headerRemoteConfig: HeaderRemoteConfig?,

    @SerializedName("operator")
    val operator: TextRemoteConfig?,

    @SerializedName("topText")
    val topTextRemoteConfig: TextRemoteConfig?,

    @SerializedName("connect")
    val connect: EngagementStatesRemoteConfig?,

    @SerializedName("snackBar")
    val snackBarRemoteConfig: SnackBarRemoteConfig?,

    @SerializedName("visitorVideo")
    val visitorVideoRemoteConfig: VisitorVideoRemoteConfig?,

    @SerializedName("mediaQualityIndicator")
    val mediaQualityIndicatorConfig: TextRemoteConfig?
) {
    fun toCallTheme(): CallTheme = CallTheme(
        background = background?.toLayerTheme(),
        bottomText = bottomTextRemoteConfig?.toTextTheme(),
        buttonBar = buttonBarRemoteConfig?.toButtonBarTheme(),
        duration = duration?.toTextTheme(),
        header = headerRemoteConfig?.toHeaderTheme(),
        operator = operator?.toTextTheme(),
        topText = topTextRemoteConfig?.toTextTheme(),
        connect = connect?.toEngagementStatesTheme(),
        snackBar = snackBarRemoteConfig?.toSnackBarTheme(),
        visitorVideo = visitorVideoRemoteConfig?.toVisitorVideoTheme(),
        mediaQualityIndicator = mediaQualityIndicatorConfig?.toTextTheme()
    )
}
