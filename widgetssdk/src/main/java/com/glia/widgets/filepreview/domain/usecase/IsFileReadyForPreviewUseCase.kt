package com.glia.widgets.filepreview.domain.usecase

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.filepreview.data.GliaFileRepository

internal class IsFileReadyForPreviewUseCase(
    private val fileRepository: GliaFileRepository
) {
    operator fun invoke(attachmentFile: AttachmentFile): Boolean =
        fileRepository.isReadyForPreview(attachmentFile)
}