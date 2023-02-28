package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.FileAttachment

class GetSecureFileAttachmentsUseCase(private val repository: SecureFileAttachmentRepository) {
    operator fun invoke(): List<FileAttachment> {
        return repository.getFileAttachments()
    }
}
