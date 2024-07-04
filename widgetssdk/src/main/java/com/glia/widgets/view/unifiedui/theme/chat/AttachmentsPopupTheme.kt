package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme

internal data class AttachmentsPopupTheme(
    val dividerColor: ColorTheme? = null,
    val background: ColorTheme? = null,
    val photoLibrary: AttachmentItemTheme? = null,
    val takePhoto: AttachmentItemTheme? = null,
    val browse: AttachmentItemTheme? = null
) : Mergeable<AttachmentsPopupTheme> {
    override fun merge(other: AttachmentsPopupTheme): AttachmentsPopupTheme = AttachmentsPopupTheme(
        dividerColor = dividerColor merge other.dividerColor,
        background = background merge other.background,
        photoLibrary = photoLibrary merge other.photoLibrary,
        takePhoto = takePhoto merge other.takePhoto,
        browse = browse merge other.browse
    )
}
