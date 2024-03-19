package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import java.util.Observer

internal class RemoveFileAttachmentObserverUseCase(private val repository: FileAttachmentRepository) {
    operator fun invoke(observer: Observer?) {
        repository.removeObserver(observer)
    }
}
