package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.secureconversations.SendMessageRepository

class ResetMessageCenterUseCase(
    private val fileAttachmentRepository: SecureFileAttachmentRepository,
    private val sendMessageRepository: SendMessageRepository
) {
    operator fun invoke() {
        fileAttachmentRepository.detachAllFiles()
        sendMessageRepository.reset()
    }
}
