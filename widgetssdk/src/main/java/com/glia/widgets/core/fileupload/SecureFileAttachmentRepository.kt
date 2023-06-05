package com.glia.widgets.core.fileupload

import android.net.Uri
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.di.GliaCore
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class SecureFileAttachmentRepository(private val gliaCore: GliaCore) {
    private val secureConversations: SecureConversations by lazy {
        gliaCore.secureConversations
    }

    private val _observable = BehaviorSubject.createDefault(emptyList<FileAttachment>())

    val observable: Observable<List<FileAttachment>> = _observable

    fun getFileAttachments(): List<FileAttachment> {
        return _observable.value ?: emptyList()
    }

    fun getReadyToSendFileAttachments(): List<FileAttachment> {
        return getFileAttachments()
            .filter { obj: FileAttachment -> obj.isReadyToSend }
    }

    fun getAttachedFilesCount(): Int {
        return getFileAttachments().size
    }

    fun isFileAttached(uri: Uri): Boolean {
        return getFileAttachments()
            .any { it.uri == uri }
    }

    fun attachFile(file: FileAttachment) {
        _observable.onNext(getFileAttachments() + file)
    }

    fun uploadFile(file: FileAttachment, listener: AddFileToAttachmentAndUploadUseCase.Listener) {
        val engagement = gliaCore.currentEngagement.orElse(null)
        if (engagement != null) {
            engagement.uploadFile(file.uri, handleFileUpload(file, listener))
        } else {
            secureConversations.uploadFile(file.uri, handleFileUpload(file, listener))
        }
    }

    private fun handleFileUpload(
        file: FileAttachment,
        listener: AddFileToAttachmentAndUploadUseCase.Listener
    ) = RequestCallback<EngagementFile> { engagementFile: EngagementFile?, e: GliaException? ->
        if (engagementFile != null) {
            if (!engagementFile.isSecurityScanRequired) {
                onUploadFileSuccess(file.uri, engagementFile, listener)
            } else {
                onUploadFileSecurityScanRequired(file.uri, engagementFile, listener)
            }
        } else if (e != null) {
            setFileAttachmentStatus(file.uri, getAttachmentStatus(e))
            listener.onError(e)
        }
    }

    fun detachFile(attachment: FileAttachment) {
        _observable.onNext(
            getFileAttachments()
                .filter { it.uri !== attachment.uri }
        )
    }

    fun detachFiles(attachments: List<FileAttachment?>) {
        _observable.onNext(
            getFileAttachments()
                .filter { attachment: FileAttachment? ->
                    !attachments.contains(
                        attachment
                    )
                }
        )
    }

    fun detachAllFiles() {
        _observable.onNext(emptyList())
    }

    private fun onUploadFileSecurityScanRequired(
        uri: Uri,
        engagementFile: EngagementFile,
        listener: AddFileToAttachmentAndUploadUseCase.Listener
    ) {
        setFileAttachmentStatus(uri, FileAttachment.Status.SECURITY_SCAN)
        listener.onSecurityCheckStarted()
        engagementFile.on(
            EngagementFile.Events.SCAN_RESULT
        ) { scanResult: EngagementFile.ScanResult? ->
            engagementFile.off(EngagementFile.Events.SCAN_RESULT)
            listener.onSecurityCheckFinished(scanResult)
            onUploadFileSecurityScanReceived(uri, engagementFile, scanResult, listener)
        }
    }

    private fun onUploadFileSecurityScanReceived(
        uri: Uri,
        engagementFile: EngagementFile?,
        scanResult: EngagementFile.ScanResult?,
        listener: AddFileToAttachmentAndUploadUseCase.Listener
    ) {
        if (scanResult == EngagementFile.ScanResult.CLEAN && engagementFile != null) {
            onUploadFileSuccess(uri, engagementFile, listener)
        } else {
            setFileAttachmentStatus(uri, FileAttachment.Status.ERROR_SECURITY_SCAN_FAILED)
            listener.onFinished()
        }
    }

    private fun onUploadFileSuccess(
        uri: Uri,
        engagementFile: EngagementFile,
        listener: AddFileToAttachmentAndUploadUseCase.Listener
    ) {
        onEngagementFileReceived(uri, engagementFile)
        listener.onFinished()
    }

    private fun onEngagementFileReceived(uri: Uri, engagementFile: EngagementFile) {
        _observable.onNext(
            getFileAttachments()
                .map { attachment: FileAttachment ->
                    if (attachment.uri == uri) {
                        attachment
                            .setEngagementFile(engagementFile)
                            .setAttachmentStatus(FileAttachment.Status.READY_TO_SEND)
                    } else {
                        attachment
                    }
                }
        )
    }

    fun setFileAttachmentTooLarge(uri: Uri) {
        setFileAttachmentStatus(uri, FileAttachment.Status.ERROR_FILE_TOO_LARGE)
    }

    fun setSupportedFileAttachmentCountExceeded(uri: Uri) {
        setFileAttachmentStatus(uri, FileAttachment.Status.ERROR_SUPPORTED_FILE_ATTACHMENT_COUNT_EXCEEDED)
    }

    private fun setFileAttachmentStatus(uri: Uri, status: FileAttachment.Status) {
        _observable.onNext(
            getFileAttachments()
                .map { fileAttachment: FileAttachment ->
                    if (fileAttachment.uri === uri) {
                        fileAttachment.setAttachmentStatus(status)
                    } else {
                        fileAttachment
                    }
                }
        )
    }

    private fun getAttachmentStatus(exception: GliaException): FileAttachment.Status {
        return when (exception.cause) {
            GliaException.Cause.FILE_UPLOAD_FORBIDDEN -> FileAttachment.Status.ERROR_FILE_UPLOAD_FORBIDDEN
            GliaException.Cause.INVALID_INPUT -> FileAttachment.Status.ERROR_INVALID_INPUT
            GliaException.Cause.NETWORK_TIMEOUT -> FileAttachment.Status.ERROR_NETWORK_TIMEOUT
            GliaException.Cause.INTERNAL_ERROR -> FileAttachment.Status.ERROR_INTERNAL
            GliaException.Cause.PERMISSIONS_DENIED -> FileAttachment.Status.ERROR_PERMISSIONS_DENIED
            GliaException.Cause.FILE_FORMAT_UNSUPPORTED -> FileAttachment.Status.ERROR_FORMAT_UNSUPPORTED
            GliaException.Cause.FILE_TOO_LARGE -> FileAttachment.Status.ERROR_FILE_TOO_LARGE
            else -> FileAttachment.Status.ERROR_UNKNOWN
        }
    }
}
