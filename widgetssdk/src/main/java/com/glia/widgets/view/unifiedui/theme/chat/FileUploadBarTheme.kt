package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.theme.base.ColorTheme

internal data class FileUploadBarTheme(
    val filePreview: FilePreviewTheme?,
    val uploading: UploadFileTheme?,
    val uploaded: UploadFileTheme?,
    val error: UploadFileTheme?,
    val progress: ColorTheme?,
    val errorProgress: ColorTheme?,
    val progressBackground: ColorTheme?,
    val removeButton: ColorTheme?
)
