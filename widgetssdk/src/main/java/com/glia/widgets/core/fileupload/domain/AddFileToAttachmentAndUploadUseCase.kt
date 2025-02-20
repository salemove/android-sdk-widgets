package com.glia.widgets.core.fileupload.domain

import com.glia.androidsdk.engagement.EngagementFile
import com.glia.widgets.core.engagement.exception.EngagementMissingException
import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.fileupload.exception.RemoveBeforeReUploadingException
import com.glia.widgets.core.fileupload.exception.SupportedFileCountExceededException
import com.glia.widgets.core.fileupload.exception.SupportedFileSizeExceededException
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.core.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase

internal class AddFileToAttachmentAndUploadUseCase(
    private val isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase
) {
    private val isSupportedFileCountExceeded: Boolean
        get() = fileAttachmentRepository.attachedFilesCount > SupportedFileCountCheckUseCase.SUPPORTED_FILE_COUNT

    private val hasNoOngoingEngagement: Boolean
        get() = !isQueueingOrEngagementUseCase.hasOngoingEngagement


    fun execute(file: LocalAttachment, listener: Listener) {
        if (fileAttachmentRepository.isFileAttached(file.uri)) {
            listener.onError(RemoveBeforeReUploadingException())
        } else {
            onFileNotAttached(file, listener)
        }
    }

    private fun onFileNotAttached(file: LocalAttachment, listener: Listener) {
        fileAttachmentRepository.attachFile(file)
        if (hasNoOngoingEngagement && !manageSecureMessagingStatusUseCase.shouldUseSecureMessagingEndpoints()) {
            fileAttachmentRepository.setFileAttachmentEngagementMissing(file.uri)
            listener.onError(EngagementMissingException())
        } else {
            onHasOngoingOrSecureEngagement(file, listener)
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
            fileAttachmentRepository.uploadFile(manageSecureMessagingStatusUseCase.shouldUseSecureMessagingEndpoints(), file, listener)
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
