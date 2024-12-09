package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.EngagementFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.LocalAttachment

internal class RemoveFileAttachmentUseCase(private val repository: EngagementFileAttachmentRepository) {
    operator fun invoke(attachment: LocalAttachment?) {
        repository.detachFile(attachment)
    }
}
