package com.glia.widgets.view.unifiedui.config.alert

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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
): Parcelable
