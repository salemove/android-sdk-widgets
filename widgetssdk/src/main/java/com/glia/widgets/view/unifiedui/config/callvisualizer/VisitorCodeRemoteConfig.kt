package com.glia.widgets.view.unifiedui.config.callvisualizer

import com.glia.widgets.view.unifiedui.config.base.*
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.VisitorCodeTheme
import com.google.gson.annotations.SerializedName

internal data class VisitorCodeRemoteConfig(

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("numberSlot")
    val numberSlot: NumberSlotRemoteConfig?,

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("loadingProgressColor")
    val progressBarColor: ColorLayerRemoteConfig?

){
    fun toVisitorCodeTheme(): VisitorCodeTheme {
        return VisitorCodeTheme(
            numberSlot?.toNumberSlotTheme(),
            background?.toLayerTheme(),
            title?.toTextTheme(),
            progressBarColor?.toColorTheme()
        )
    }
}
