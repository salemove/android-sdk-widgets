package com.glia.widgets.internal.secureconversations.domain

import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.internal.fileupload.domain.AddFileToAttachmentAndUploadUseCase.Listener
import com.glia.widgets.internal.fileupload.domain.FileUploadLimitNotExceededObservableUseCase
import com.glia.widgets.internal.fileupload.exception.RemoveBeforeReUploadingException
import com.glia.widgets.internal.fileupload.exception.SupportedFileCountExceededException
import com.glia.widgets.internal.fileupload.exception.SupportedFileSizeExceededException
import com.glia.widgets.internal.fileupload.model.LocalAttachment

internal class AddSecureFileToAttachmentAndUploadUseCase(private val fileAttachmentRepository: FileAttachmentRepository) {

    operator fun invoke(file: LocalAttachment, listener: Listener) {
        if (fileAttachmentRepository.isFileAttached(file.uri)) {
            listener.onError(RemoveBeforeReUploadingException())
        } else {
            onFileNotAttached(file, listener)
        }
    }

    private fun onFileNotAttached(
        file: LocalAttachment,
        listener: Listener
    ) {
        fileAttachmentRepository.attachFile(file)
        if (isSupportedFileCountExceeded()) {
            fileAttachmentRepository.setSupportedFileAttachmentCountExceeded(file.uri)
            listener.onError(SupportedFileCountExceededException())
        } else if (isSupportedFileSizeExceeded(file)) {
            fileAttachmentRepository.setFileAttachmentTooLarge(file.uri)
            listener.onError(SupportedFileSizeExceededException())
        } else {
            listener.onStarted()
            fileAttachmentRepository.uploadFile(true, file, listener)
        }
    }

    private fun isSupportedFileSizeExceeded(file: LocalAttachment): Boolean {
        return file.size >= AddFileToAttachmentAndUploadUseCase.SUPPORTED_FILE_SIZE
    }

    private fun isSupportedFileCountExceeded(): Boolean {
        return fileAttachmentRepository.getAttachedFilesCount() > FileUploadLimitNotExceededObservableUseCase.FILE_UPLOAD_LIMIT
    }
}
