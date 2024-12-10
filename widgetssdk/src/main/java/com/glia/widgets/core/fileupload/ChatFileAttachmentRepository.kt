package com.glia.widgets.core.fileupload

import android.net.Uri
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.engagement.EngagementRepository
import io.reactivex.rxjava3.core.Observable

internal class ChatFileAttachmentRepository(
    private val engagementRepository: EngagementRepository,
    private val engagementFileAttachmentRepository: FileAttachmentRepository,
    private val secureFileAttachmentRepository: FileAttachmentRepository
) : FileAttachmentRepository {
    private val fileAttachmentRepository: FileAttachmentRepository
        get() = when {
            engagementRepository.isQueueingOrLiveEngagement -> engagementFileAttachmentRepository
            else -> secureFileAttachmentRepository
        }

    override val observable: Observable<List<LocalAttachment>>
        get() = engagementRepository.engagementState
            .map { state ->
                when {
                    state.isQueueing || state.isLiveEngagement -> engagementFileAttachmentRepository
                    else -> secureFileAttachmentRepository
                }
            }
            .distinctUntilChanged()
            .toObservable()
            .flatMap { it.observable }

    override fun getFileAttachments(): List<LocalAttachment> {
        return fileAttachmentRepository.getFileAttachments()
    }

    override fun getReadyToSendFileAttachments(): List<LocalAttachment> {
        return fileAttachmentRepository.getReadyToSendFileAttachments()
    }

    override fun getAttachedFilesCount(): Int {
        return fileAttachmentRepository.getAttachedFilesCount()
    }

    override fun isFileAttached(uri: Uri): Boolean {
        return fileAttachmentRepository.isFileAttached(uri)
    }

    override fun attachFile(file: LocalAttachment) {
        fileAttachmentRepository.attachFile(file)
    }

    override fun uploadFile(file: LocalAttachment, listener: AddFileToAttachmentAndUploadUseCase.Listener) {
        fileAttachmentRepository.uploadFile(file, listener)
    }

    override fun detachFile(attachment: LocalAttachment?) {
        fileAttachmentRepository.detachFile(attachment)
    }

    override fun detachFiles(attachments: List<LocalAttachment?>) {
        fileAttachmentRepository.detachFiles(attachments)
    }

    override fun detachAllFiles() {
        fileAttachmentRepository.detachAllFiles()
    }

    override fun setFileAttachmentTooLarge(uri: Uri) {
        fileAttachmentRepository.setFileAttachmentTooLarge(uri)
    }

    override fun setSupportedFileAttachmentCountExceeded(uri: Uri) {
        fileAttachmentRepository.setSupportedFileAttachmentCountExceeded(uri)
    }
}
