package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.EngagementFileAttachmentRepository

internal class SupportedFileCountCheckUseCase(private val repository: EngagementFileAttachmentRepository) {
    operator fun invoke(): Boolean {
        return repository.getAttachedFilesCount() <= SUPPORTED_FILE_COUNT
    }

    companion object {
        const val SUPPORTED_FILE_COUNT: Long = 25
    }
}
