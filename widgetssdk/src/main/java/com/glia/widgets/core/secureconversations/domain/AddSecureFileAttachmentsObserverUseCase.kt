package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.Observable

internal class AddSecureFileAttachmentsObserverUseCase(
    private val repository: SecureFileAttachmentRepository,
    private val schedulers: Schedulers
) {
    operator fun invoke(): Observable<List<FileAttachment>> = repository.observable
        .subscribeOn(schedulers.computationScheduler)
        .observeOn(schedulers.mainScheduler)
}
