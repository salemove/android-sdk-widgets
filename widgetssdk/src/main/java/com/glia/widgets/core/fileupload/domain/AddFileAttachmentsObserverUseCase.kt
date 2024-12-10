package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.rxjava3.core.Observable

/**
 * @hide
 */
internal class AddFileAttachmentsObserverUseCase(
    private val fileAttachmentUseCase: ChatFileAttachmentRepositoryUseCase,
    private val schedulers: Schedulers
) {
    operator fun invoke(): Observable<List<LocalAttachment>> = fileAttachmentUseCase.flowable()
        .subscribeOn(schedulers.computationScheduler)
        .observeOn(schedulers.mainScheduler)
        .toObservable()
        .flatMap { it.observable }
}
