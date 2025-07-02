package com.glia.widgets.internal.fileupload

import android.net.Uri
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.di.GliaCore
import com.glia.widgets.internal.engagement.exception.EngagementMissingException
import com.glia.widgets.internal.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.jvm.optionals.getOrNull

internal interface FileAttachmentRepository {
    val observable: Observable<List<LocalAttachment>>

    fun getFileAttachments(): List<LocalAttachment>
    fun getReadyToSendFileAttachments(): List<LocalAttachment>
    fun getAttachedFilesCount(): Int
    fun isFileAttached(uri: Uri): Boolean
    fun attachFile(file: LocalAttachment)
    fun uploadFile(shouldUseSecureMessagingEndpoints: Boolean, file: LocalAttachment, listener: AddFileToAttachmentAndUploadUseCase.Listener)
    fun detachFile(attachment: LocalAttachment)
    fun detachFiles(attachments: List<LocalAttachment>)
    fun detachAllFiles()
    fun setFileAttachmentTooLarge(uri: Uri)
    fun setSupportedFileAttachmentCountExceeded(uri: Uri)
    fun setFileAttachmentEngagementMissing(uri: Uri)
}

internal class FileAttachmentRepositoryImpl(
    private val gliaCore: GliaCore
) : FileAttachmentRepository {
    private val secureConversations: SecureConversations by lazy {
        gliaCore.secureConversations
    }

    private val _observable = BehaviorSubject.createDefault(emptyList<LocalAttachment>())

    override val observable: Observable<List<LocalAttachment>> = _observable

    override fun getFileAttachments(): List<LocalAttachment> {
        return _observable.value ?: emptyList()
    }

    override fun getReadyToSendFileAttachments(): List<LocalAttachment> {
        return getFileAttachments()
            .filter { obj: LocalAttachment -> obj.isReadyToSend }
    }

    override fun getAttachedFilesCount(): Int {
        return getFileAttachments().size
    }

    override fun isFileAttached(uri: Uri): Boolean {
        return getFileAttachments()
            .any { it.uri == uri }
    }

    override fun attachFile(file: LocalAttachment) {
        _observable.onNext(getFileAttachments() + file)
    }

    override fun uploadFile(
        shouldUseSecureMessagingEndpoints: Boolean,
        file: LocalAttachment,
        listener: AddFileToAttachmentAndUploadUseCase.Listener
    ) {
        if (shouldUseSecureMessagingEndpoints) {
            uploadFileForSecureConversation(file, listener)
        } else {
            uploadFileForLiveEngagement(file, listener)
        }
    }

    private fun uploadFileForSecureConversation(file: LocalAttachment, listener: AddFileToAttachmentAndUploadUseCase.Listener) {
        secureConversations.uploadFile(file.uri, handleFileUpload(file, listener))
    }

    private fun uploadFileForLiveEngagement(file: LocalAttachment, listener: AddFileToAttachmentAndUploadUseCase.Listener) {
        val engagement = gliaCore.currentEngagement.getOrNull()
        when {
            engagement != null -> {
                engagement.uploadFile(file.uri, handleFileUpload(file, listener))
            }

            else -> {
                setFileAttachmentEngagementMissing(file.uri)
                listener.onError(EngagementMissingException())
            }
        }
    }

    private fun handleFileUpload(
        file: LocalAttachment,
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

    override fun detachFile(attachment: LocalAttachment) {
        _observable.onNext(
            getFileAttachments()
                .filter { it.uri !== attachment.uri }
        )
    }

    override fun detachFiles(attachments: List<LocalAttachment>) {
        _observable.onNext(
            getFileAttachments()
                .filter { attachment: LocalAttachment? ->
                    !attachments.contains(
                        attachment
                    )
                }
        )
    }

    override fun detachAllFiles() {
        _observable.onNext(emptyList())
    }

    private fun onUploadFileSecurityScanRequired(
        uri: Uri,
        engagementFile: EngagementFile,
        listener: AddFileToAttachmentAndUploadUseCase.Listener
    ) {
        setFileAttachmentStatus(uri, LocalAttachment.Status.SECURITY_SCAN)
        listener.onSecurityCheckStarted()
        engagementFile.on(EngagementFile.Events.SCAN_RESULT) { scanResult: EngagementFile.ScanResult? ->
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
            setFileAttachmentStatus(uri, LocalAttachment.Status.ERROR_SECURITY_SCAN_FAILED)
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

    private fun setFileAttachmentStatus(uri: Uri, status: LocalAttachment.Status) {
        _observable.onNext(
            getFileAttachments()
                .map { localAttachment: LocalAttachment ->
                    if (localAttachment.uri === uri) {
                        localAttachment.copy(attachmentStatus = status)
                    } else {
                        localAttachment
                    }
                }
        )
    }

    private fun onEngagementFileReceived(uri: Uri, engagementFile: EngagementFile) {
        _observable.onNext(
            getFileAttachments()
                .map { attachment: LocalAttachment ->
                    if (attachment.uri == uri) {
                        attachment.copy(engagementFile = engagementFile, attachmentStatus = LocalAttachment.Status.READY_TO_SEND)
                    } else {
                        attachment
                    }
                }
        )
    }

    override fun setFileAttachmentTooLarge(uri: Uri) {
        setFileAttachmentStatus(uri, LocalAttachment.Status.ERROR_FILE_TOO_LARGE)
    }

    override fun setSupportedFileAttachmentCountExceeded(uri: Uri) {
        setFileAttachmentStatus(uri, LocalAttachment.Status.ERROR_SUPPORTED_FILE_ATTACHMENT_COUNT_EXCEEDED)
    }

    override fun setFileAttachmentEngagementMissing(uri: Uri) {
        setFileAttachmentStatus(uri, LocalAttachment.Status.ERROR_ENGAGEMENT_MISSING)
    }

    private fun getAttachmentStatus(exception: GliaException): LocalAttachment.Status {
        return when (exception.cause) {
            GliaException.Cause.FILE_UPLOAD_FORBIDDEN -> LocalAttachment.Status.ERROR_FILE_UPLOAD_FORBIDDEN
            GliaException.Cause.INVALID_INPUT -> LocalAttachment.Status.ERROR_INVALID_INPUT
            GliaException.Cause.NETWORK_TIMEOUT -> LocalAttachment.Status.ERROR_NETWORK_TIMEOUT
            GliaException.Cause.INTERNAL_ERROR -> LocalAttachment.Status.ERROR_INTERNAL
            GliaException.Cause.PERMISSIONS_DENIED -> LocalAttachment.Status.ERROR_PERMISSIONS_DENIED
            GliaException.Cause.FILE_FORMAT_UNSUPPORTED -> LocalAttachment.Status.ERROR_FORMAT_UNSUPPORTED
            GliaException.Cause.FILE_TOO_LARGE -> LocalAttachment.Status.ERROR_FILE_TOO_LARGE
            else -> LocalAttachment.Status.ERROR_UNKNOWN
        }
    }
}
