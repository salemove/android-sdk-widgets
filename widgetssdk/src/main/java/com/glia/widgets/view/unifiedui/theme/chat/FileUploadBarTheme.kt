package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
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
) : Mergeable<FileUploadBarTheme> {
    override fun merge(other: FileUploadBarTheme): FileUploadBarTheme = FileUploadBarTheme(
        filePreview = filePreview merge other.filePreview,
        uploading = uploading merge other.uploading,
        uploaded = uploaded merge other.uploaded,
        error = error merge other.error,
        progress = progress merge other.progress,
        errorProgress = errorProgress merge other.errorProgress,
        progressBackground = progressBackground merge other.progressBackground,
        removeButton = removeButton merge other.removeButton
    )
}
