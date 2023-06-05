package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase
import com.glia.widgets.core.fileupload.exception.RemoveBeforeReUploadingException
import com.glia.widgets.core.fileupload.exception.SupportedFileCountExceededException
import com.glia.widgets.core.fileupload.exception.SupportedFileSizeExceededException
import com.glia.widgets.core.fileupload.model.FileAttachment

class AddSecureFileToAttachmentAndUploadUseCase(private val fileAttachmentRepository: SecureFileAttachmentRepository) {

    fun execute(file: FileAttachment, listener: AddFileToAttachmentAndUploadUseCase.Listener) {
        if (fileAttachmentRepository.isFileAttached(file.uri)) {
            listener.onError(RemoveBeforeReUploadingException())
        } else {
            onFileNotAttached(file, listener)
        }
    }

    private fun onFileNotAttached(
        file: FileAttachment,
        listener: AddFileToAttachmentAndUploadUseCase.Listener
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
            fileAttachmentRepository.uploadFile(file, listener)
        }
    }

    private fun isSupportedFileSizeExceeded(file: FileAttachment): Boolean {
        return file.size >= AddFileToAttachmentAndUploadUseCase.SUPPORTED_FILE_SIZE
    }
    private fun isSupportedFileCountExceeded(): Boolean {
        return fileAttachmentRepository.getAttachedFilesCount() > SupportedFileCountCheckUseCase.SUPPORTED_FILE_COUNT
    }
}
