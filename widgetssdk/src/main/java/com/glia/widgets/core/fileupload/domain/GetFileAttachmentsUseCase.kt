package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.fileupload.model.FileAttachment

internal class GetFileAttachmentsUseCase(private val repository: FileAttachmentRepository) {
    operator fun invoke(): List<FileAttachment> {
        return repository.fileAttachments
    }
}
