package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.rxjava3.core.Observable

internal class AddSecureFileAttachmentsObserverUseCase(
    private val repository: SecureFileAttachmentRepository,
    private val schedulers: Schedulers
) {
    operator fun invoke(): Observable<List<LocalAttachment>> = repository.observable
        .subscribeOn(schedulers.computationScheduler)
        .observeOn(schedulers.mainScheduler)
}
