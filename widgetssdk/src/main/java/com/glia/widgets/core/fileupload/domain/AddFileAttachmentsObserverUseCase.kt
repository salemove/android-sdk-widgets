package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.helper.rx.Schedulers
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