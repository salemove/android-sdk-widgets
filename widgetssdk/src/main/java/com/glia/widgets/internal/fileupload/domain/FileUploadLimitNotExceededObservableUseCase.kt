package com.glia.widgets.internal.fileupload.domain

import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import io.reactivex.rxjava3.core.Observable

internal class FileUploadLimitNotExceededObservableUseCase(private val repository: FileAttachmentRepository) {

    operator fun invoke(): Observable<Boolean> = repository.observable
        .map { it.count() < FILE_UPLOAD_LIMIT }

    companion object {
        const val FILE_UPLOAD_LIMIT: Int = 25
    }
}
