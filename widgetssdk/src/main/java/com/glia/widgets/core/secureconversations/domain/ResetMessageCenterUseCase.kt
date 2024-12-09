package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.secureconversations.SendMessageRepository

internal class ResetMessageCenterUseCase(
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val sendMessageRepository: SendMessageRepository
) {
    operator fun invoke() {
        fileAttachmentRepository.detachAllFiles()
        sendMessageRepository.reset()
    }
}
