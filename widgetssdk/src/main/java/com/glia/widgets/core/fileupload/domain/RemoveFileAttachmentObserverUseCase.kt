package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.EngagementFileAttachmentRepository
import java.util.Observer

@Deprecated("This class is not needed anymore")
internal class RemoveFileAttachmentObserverUseCase(private val repository: EngagementFileAttachmentRepository) {
    operator fun invoke(observer: Observer?) {
        repository.removeObserver(observer)
    }
}
