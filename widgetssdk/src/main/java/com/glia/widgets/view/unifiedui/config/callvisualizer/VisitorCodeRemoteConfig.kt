package com.glia.widgets.view.unifiedui.config.callvisualizer

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.VisitorCodeTheme
import com.google.gson.annotations.SerializedName

internal data class VisitorCodeRemoteConfig(

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("numberSlotText")
    val numberSlotText: TextRemoteConfig?,

    @SerializedName("numberSlotBackground")
    val numberSlotBackground: LayerRemoteConfig?,

    @SerializedName("closeButtonColor")
    val closeButtonColor: ColorLayerRemoteConfig?,

    @SerializedName("actionButton")
    val refreshButton: ButtonRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("loadingProgressColor")
    val progressBarColor: ColorLayerRemoteConfig?

) {
    fun toVisitorCodeTheme(): VisitorCodeTheme {
        return VisitorCodeTheme(
            numberSlotText?.toTextTheme(),
            numberSlotBackground?.toLayerTheme(),
            closeButtonColor?.toColorTheme(),
            refreshButton?.toButtonTheme(),
            background?.toLayerTheme(),
            title?.toTextTheme(),
            progressBarColor?.toColorTheme()
        )
    }
}
