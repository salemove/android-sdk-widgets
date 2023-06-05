package com.glia.widgets.view.unifiedui.config.survey

import com.glia.widgets.view.unifiedui.config.base.FontRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.survey.OptionButtonTheme
import com.google.gson.annotations.SerializedName

internal data class OptionButtonRemoteConfig(

    @SerializedName("normalText")
    val normalTextRemoteConfig: TextRemoteConfig?,

    @SerializedName("normalLayer")
    val normalLayerRemoteConfig: LayerRemoteConfig?,

    @SerializedName("selectedText")
    val selectedTextRemoteConfig: TextRemoteConfig?,

    @SerializedName("selectedLayer")
    val selectedLayerRemoteConfig: LayerRemoteConfig?,

    @SerializedName("highlightedText")
    val highlightedTextRemoteConfig: TextRemoteConfig?,

    @SerializedName("highlightedLayer")
    val highlightedLayerRemoteConfig: LayerRemoteConfig?,

    @SerializedName("font")
    val fontRemoteConfig: FontRemoteConfig?
) {
    fun toOptionButtonTheme(): OptionButtonTheme = OptionButtonTheme(
        normalText = normalTextRemoteConfig?.toTextTheme(),
        normalLayer = normalLayerRemoteConfig?.toLayerTheme(),
        selectedText = selectedTextRemoteConfig?.toTextTheme(),
        selectedLayer = selectedLayerRemoteConfig?.toLayerTheme(),
        highlightedText = highlightedTextRemoteConfig?.toTextTheme(),
        highlightedLayer = highlightedLayerRemoteConfig?.toLayerTheme(),
        fontSize = fontRemoteConfig?.size?.value,
        fontStyle = fontRemoteConfig?.style?.style
    )
}
