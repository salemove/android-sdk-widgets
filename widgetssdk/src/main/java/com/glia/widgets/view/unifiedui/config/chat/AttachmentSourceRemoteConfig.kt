package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AttachmentSourceRemoteConfig(

    @SerializedName("type")
    val type: AttachmentSourceTypeRemoteConfig = AttachmentSourceTypeRemoteConfig.PHOTO_LIBRARY,

    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("tintColor")
    val tintColor: ColorLayerRemoteConfig?
) : Parcelable
