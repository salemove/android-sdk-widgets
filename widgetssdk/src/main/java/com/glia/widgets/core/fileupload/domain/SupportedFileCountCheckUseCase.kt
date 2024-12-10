package com.glia.widgets.core.fileupload.domain

internal class SupportedFileCountCheckUseCase(private val fileAttachmentUseCase: ChatFileAttachmentRepositoryUseCase) {
    operator fun invoke(): Boolean {
        return fileAttachmentUseCase().getAttachedFilesCount() <= SUPPORTED_FILE_COUNT
    }

    companion object {
        const val SUPPORTED_FILE_COUNT: Long = 25
    }
}
