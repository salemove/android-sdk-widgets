package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.FileAttachment

internal class RemoveSecureFileAttachmentUseCase(private val repository: SecureFileAttachmentRepository) {
    fun execute(attachment: FileAttachment) {
        repository.detachFile(attachment)
    }
}
