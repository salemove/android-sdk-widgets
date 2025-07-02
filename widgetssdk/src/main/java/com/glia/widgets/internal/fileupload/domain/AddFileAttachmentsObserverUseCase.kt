package com.glia.widgets.internal.fileupload.domain

import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import io.reactivex.rxjava3.core.Observable

/**
 * @hide
 */
internal class AddFileAttachmentsObserverUseCase(
    private val repository: FileAttachmentRepository,
    private val schedulers: Schedulers
) {
    operator fun invoke(): Observable<List<LocalAttachment>> = repository.observable
        .subscribeOn(schedulers.computationScheduler)
        .observeOn(schedulers.mainScheduler)
}
