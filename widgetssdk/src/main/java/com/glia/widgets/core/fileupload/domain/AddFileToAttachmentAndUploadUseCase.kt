package com.glia.widgets.core.fileupload.domain

import com.glia.androidsdk.engagement.EngagementFile
import com.glia.widgets.core.engagement.exception.EngagementMissingException
import com.glia.widgets.core.fileupload.exception.RemoveBeforeReUploadingException
import com.glia.widgets.core.fileupload.exception.SupportedFileCountExceededException
import com.glia.widgets.core.fileupload.exception.SupportedFileSizeExceededException
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.core.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase

internal class AddFileToAttachmentAndUploadUseCase(
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val fileAttachmentUseCase: ChatFileAttachmentRepositoryUseCase,
    private val manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase
) {
    private val isSupportedFileCountExceeded: Boolean
        get() = fileAttachmentUseCase().getAttachedFilesCount() > SupportedFileCountCheckUseCase.SUPPORTED_FILE_COUNT

    fun execute(file: LocalAttachment, listener: Listener) {
        if (fileAttachmentUseCase().isFileAttached(file.uri)) {
            listener.onError(RemoveBeforeReUploadingException())
        } else {
            onFileNotAttached(file, listener)
        }
    }

    private fun onFileNotAttached(file: LocalAttachment, listener: Listener) {
        fileAttachmentUseCase().attachFile(file)
        if (isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement || manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) {
            onHasOngoingOrSecureEngagement(file, listener)
        } else {
            fileAttachmentUseCase().setFileAttachmentEngagementMissing(file.uri)
            listener.onError(EngagementMissingException())
        }
    }

    private fun onHasOngoingOrSecureEngagement(file: LocalAttachment, listener: Listener) {
        if (isSupportedFileCountExceeded) {
            fileAttachmentUseCase().setSupportedFileAttachmentCountExceeded(file.uri)
            listener.onError(SupportedFileCountExceededException())
        } else if (isSupportedFileSizeExceeded(file)) {
            fileAttachmentUseCase().setFileAttachmentTooLarge(file.uri)
            listener.onError(SupportedFileSizeExceededException())
        } else {
            listener.onStarted()
            fileAttachmentUseCase().uploadFile(file, listener)
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
