package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.theme.base.ColorTheme

internal data class FileUploadBarTheme(
    val filePreview: FilePreviewTheme? = null,
    val uploading: UploadFileTheme? = null,
    val uploaded: UploadFileTheme? = null,
    val error: UploadFileTheme? = null,
    val progress: ColorTheme? = null,
    val errorProgress: ColorTheme? = null,
    val progressBackground: ColorTheme? = null,
    val removeButton: ColorTheme? = null
)
