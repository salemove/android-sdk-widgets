package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.EngagementFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.LocalAttachment

internal class RemoveFileAttachmentUseCase(private val fileAttachmentUseCase: ChatFileAttachmentRepositoryUseCase) {
    operator fun invoke(attachment: LocalAttachment?) {
        fileAttachmentUseCase().detachFile(attachment)
    }
}
