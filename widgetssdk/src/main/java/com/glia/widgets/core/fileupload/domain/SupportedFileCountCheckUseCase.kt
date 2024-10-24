package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository

internal class SupportedFileCountCheckUseCase(private val repository: FileAttachmentRepository) {
    operator fun invoke(): Boolean {
        return repository.localAttachments.size <= SUPPORTED_FILE_COUNT
    }

    companion object {
        const val SUPPORTED_FILE_COUNT: Long = 25
    }
}
