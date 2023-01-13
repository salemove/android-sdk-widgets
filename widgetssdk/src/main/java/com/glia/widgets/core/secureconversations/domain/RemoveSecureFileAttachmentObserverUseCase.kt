package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import java.util.*

class RemoveSecureFileAttachmentObserverUseCase(private val repository: SecureFileAttachmentRepository) {
    fun execute(observer: Observer?) {
        repository.removeObserver(observer)
    }
}