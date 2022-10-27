package com.glia.widgets.view.unifiedui.config.survey

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.FontRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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
    val fontRemoteConfig: FontRemoteConfig?,
) : Parcelable
