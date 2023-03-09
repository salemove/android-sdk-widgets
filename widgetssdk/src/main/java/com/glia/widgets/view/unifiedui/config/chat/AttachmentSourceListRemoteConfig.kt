package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.AttachmentsPopupTheme
import com.google.gson.annotations.SerializedName

internal data class AttachmentSourceListRemoteConfig(

    @SerializedName("separator")
    val separator: ColorLayerRemoteConfig?,

    @SerializedName("background")
    val background: ColorLayerRemoteConfig?,

    @SerializedName("items")
    val items: List<AttachmentSourceRemoteConfig>?,
) {

    private val photoLibrary: AttachmentSourceRemoteConfig?
        get() = items?.firstOrNull { it.type == AttachmentSourceTypeRemoteConfig.PHOTO_LIBRARY }

    private val takePhoto: AttachmentSourceRemoteConfig?
        get() = items?.firstOrNull { it.type == AttachmentSourceTypeRemoteConfig.TAKE_PHOTO }

    private val browse: AttachmentSourceRemoteConfig?
        get() = items?.firstOrNull { it.type == AttachmentSourceTypeRemoteConfig.BROWSE }

    fun toAttachmentsPopupTheme(): AttachmentsPopupTheme = AttachmentsPopupTheme(
        dividerColor = separator?.toColorTheme(),
        background = background?.toColorTheme(),
        photoLibrary = photoLibrary?.toAttachmentItemTheme(),
        takePhoto = takePhoto?.toAttachmentItemTheme(),
        browse = browse?.toAttachmentItemTheme()
    )
}
