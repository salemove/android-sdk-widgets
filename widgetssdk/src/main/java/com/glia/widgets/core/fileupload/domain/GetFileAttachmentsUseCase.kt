package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.model.LocalAttachment

internal class GetFileAttachmentsUseCase(private val fileAttachmentUseCase: ChatFileAttachmentRepositoryUseCase) {
    operator fun invoke(): List<LocalAttachment> {
        return fileAttachmentUseCase().getFileAttachments()
    }
}
