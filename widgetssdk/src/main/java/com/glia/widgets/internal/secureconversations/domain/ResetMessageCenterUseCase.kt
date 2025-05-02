package com.glia.widgets.internal.secureconversations.domain

import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.secureconversations.SendMessageRepository

internal class ResetMessageCenterUseCase(
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val sendMessageRepository: SendMessageRepository
) {
    operator fun invoke() {
        fileAttachmentRepository.detachAllFiles()
        sendMessageRepository.reset()
    }
}
