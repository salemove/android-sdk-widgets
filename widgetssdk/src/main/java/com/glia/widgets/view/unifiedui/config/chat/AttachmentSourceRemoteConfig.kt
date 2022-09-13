package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.AttachmentsItemTheme
import com.google.gson.annotations.SerializedName

internal data class AttachmentSourceRemoteConfig(

    @SerializedName("type")
    val type: AttachmentSourceTypeRemoteConfig = AttachmentSourceTypeRemoteConfig.PHOTO_LIBRARY,

    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("tintColor")
    val tintColor: ColorLayerRemoteConfig?
) {
    fun toAttachmentsItemTheme(): AttachmentsItemTheme = AttachmentsItemTheme(
        text = textRemoteConfig?.toTextTheme(),
        iconColor = tintColor?.toColorTheme()
    )
}
