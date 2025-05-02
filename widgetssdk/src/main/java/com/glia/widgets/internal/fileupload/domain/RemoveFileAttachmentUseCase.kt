package com.glia.widgets.internal.fileupload.domain

import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.fileupload.model.LocalAttachment

internal class RemoveFileAttachmentUseCase(private val repository: FileAttachmentRepository) {
    operator fun invoke(attachment: LocalAttachment) {
        repository.detachFile(attachment)
    }
}
