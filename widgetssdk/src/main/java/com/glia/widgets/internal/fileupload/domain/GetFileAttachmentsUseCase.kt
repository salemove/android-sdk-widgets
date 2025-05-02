package com.glia.widgets.internal.fileupload.domain

import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.fileupload.model.LocalAttachment

internal class GetFileAttachmentsUseCase(private val repository: FileAttachmentRepository) {
    operator fun invoke(): List<LocalAttachment> {
        return repository.getFileAttachments()
    }
}
