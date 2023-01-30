package com.glia.widgets.core.fileupload.domain

import com.glia.androidsdk.engagement.EngagementFile
import com.glia.widgets.core.engagement.GliaEngagementRepository
import com.glia.widgets.core.engagement.GliaEngagementTypeRepository
import com.glia.widgets.core.engagement.exception.EngagementMissingException
import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.fileupload.exception.RemoveBeforeReUploadingException
import com.glia.widgets.core.fileupload.exception.SupportedFileCountExceededException
import com.glia.widgets.core.fileupload.exception.SupportedFileSizeExceededException
import com.glia.widgets.core.fileupload.model.FileAttachment

class AddFileToAttachmentAndUploadUseCase(
    private val gliaEngagementRepository: GliaEngagementRepository,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val engagementTypeRepository: GliaEngagementTypeRepository
) {
    private val isSupportedFileCountExceeded: Boolean
        get() = fileAttachmentRepository.attachedFilesCount > SupportedFileCountCheckUseCase.SUPPORTED_FILE_COUNT

    private val hasNoOngoingEngagement: Boolean
        get() = !gliaEngagementRepository.hasOngoingEngagement()

    private val isNotSecureEngagement: Boolean
        get() = !engagementTypeRepository.isSecureEngagement

    fun execute(file: FileAttachment, listener: Listener) {
        if (fileAttachmentRepository.isFileAttached(file.uri)) {
            listener.onError(RemoveBeforeReUploadingException())
        } else {
            onFileNotAttached(file, listener)
        }
    }

    private fun onFileNotAttached(file: FileAttachment, listener: Listener) {
        fileAttachmentRepository.attachFile(file)
        if (hasNoOngoingEngagement && isNotSecureEngagement) {
            fileAttachmentRepository.setFileAttachmentEngagementMissing(file.uri)
            listener.onError(EngagementMissingException())
        } else {
            onHasOngoingOrSecureEngagement(file, listener)
        }
    }

    private fun onHasOngoingOrSecureEngagement(file: FileAttachment, listener: Listener) {
        if (isSupportedFileCountExceeded) {
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