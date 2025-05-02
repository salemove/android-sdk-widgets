package com.glia.widgets.internal.fileupload.domain

import com.glia.androidsdk.engagement.EngagementFile
import com.glia.widgets.internal.engagement.exception.EngagementMissingException
import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.fileupload.exception.RemoveBeforeReUploadingException
import com.glia.widgets.internal.fileupload.exception.SupportedFileCountExceededException
import com.glia.widgets.internal.fileupload.exception.SupportedFileSizeExceededException
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.internal.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase

internal class AddFileToAttachmentAndUploadUseCase(
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase
) {
    private val isSupportedFileCountExceeded: Boolean
        get() = fileAttachmentRepository.getAttachedFilesCount() > FileUploadLimitNotExceededObservableUseCase.FILE_UPLOAD_LIMIT

    operator fun invoke(file: LocalAttachment, listener: Listener) {
        if (fileAttachmentRepository.isFileAttached(file.uri)) {
            listener.onError(RemoveBeforeReUploadingException())
        } else {
            onFileNotAttached(file, listener)
        }
    }

    private fun onFileNotAttached(file: LocalAttachment, listener: Listener) {
        fileAttachmentRepository.attachFile(file)
        if (isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement || manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) {
            onHasOngoingOrSecureEngagement(file, listener)
        } else {
            fileAttachmentRepository.setFileAttachmentEngagementMissing(file.uri)
            listener.onError(EngagementMissingException())
        }
    }

    private fun onHasOngoingOrSecureEngagement(file: LocalAttachment, listener: Listener) {
        if (isSupportedFileCountExceeded) {
            fileAttachmentRepository.setSupportedFileAttachmentCountExceeded(file.uri)
            listener.onError(SupportedFileCountExceededException())
        } else if (isSupportedFileSizeExceeded(file)) {
            fileAttachmentRepository.setFileAttachmentTooLarge(file.uri)
            listener.onError(SupportedFileSizeExceededException())
        } else {
            listener.onStarted()
            fileAttachmentRepository.uploadFile(manageSecureMessagingStatusUseCase.shouldUseSecureMessagingEndpoints, file, listener)
        }
    }

    private fun isSupportedFileSizeExceeded(file: LocalAttachment): Boolean {
        return file.size >= SUPPORTED_FILE_SIZE
    }

    interface Listener {
        fun onFinished()
        fun onStarted()
        fun onError(ex: Exception)
        fun onSecurityCheckStarted()
        fun onSecurityCheckFinished(scanResult: EngagementFile.ScanResult?)
    }

    companion object {
        const val SUPPORTED_FILE_SIZE = 26214400L
    }
}
