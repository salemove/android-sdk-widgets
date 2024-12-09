package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.EngagementFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.LocalAttachment

internal class GetFileAttachmentsUseCase(private val repository: EngagementFileAttachmentRepository) {
    operator fun invoke(): List<LocalAttachment> {
        return repository.getFileAttachments()
    }
}
