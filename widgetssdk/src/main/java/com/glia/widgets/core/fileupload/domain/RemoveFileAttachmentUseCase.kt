package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.fileupload.model.FileAttachment

internal class RemoveFileAttachmentUseCase(private val repository: FileAttachmentRepository) {
    operator fun invoke(attachment: FileAttachment?) {
        repository.detachFile(attachment)
    }
}
