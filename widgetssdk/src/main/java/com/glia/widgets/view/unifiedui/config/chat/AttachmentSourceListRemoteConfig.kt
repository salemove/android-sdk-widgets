package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AttachmentSourceListRemoteConfig(

    @SerializedName("separator")
    val separator: ColorLayerRemoteConfig?,

    @SerializedName("background")
    val background: ColorLayerRemoteConfig?,

    @SerializedName("items")
    val items: List<AttachmentSourceRemoteConfig>?,
) : Parcelable {

    val photoLibrary: AttachmentSourceRemoteConfig?
        get() = items?.firstOrNull { it.type == AttachmentSourceTypeRemoteConfig.PHOTO_LIBRARY }

    val takePhoto: AttachmentSourceRemoteConfig?
        get() = items?.firstOrNull { it.type == AttachmentSourceTypeRemoteConfig.TAKE_PHOTO }

    val browse: AttachmentSourceRemoteConfig?
        get() = items?.firstOrNull { it.type == AttachmentSourceTypeRemoteConfig.BROWSE }
}
