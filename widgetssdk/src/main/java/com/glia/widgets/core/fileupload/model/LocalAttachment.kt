package com.glia.widgets.core.fileupload.model

import android.net.Uri
import com.glia.androidsdk.engagement.EngagementFile

internal data class LocalAttachment(
    val uri: Uri,
    val mimeType: String?,
    val displayName: String,
    val size: Long,
    val attachmentStatus: Status = Status.UPLOADING,
    val engagementFile: EngagementFile? = null,
) {

    constructor(attachment: LocalAttachment, status: Status) : this(
        uri = attachment.uri,
        engagementFile = attachment.engagementFile,
        displayName = attachment.displayName,
        size = attachment.size,
        attachmentStatus = status,
        mimeType = attachment.mimeType,
    )

    constructor(attachment: LocalAttachment, engagementFile: EngagementFile?) : this(
        uri = attachment.uri,
        attachmentStatus = attachment.attachmentStatus,
        displayName = attachment.displayName,
        size = attachment.size,
        engagementFile = engagementFile,
        mimeType = attachment.mimeType
    )

    fun setEngagementFile(engagementFile: EngagementFile?): LocalAttachment {
        return LocalAttachment(this, engagementFile)
    }

    fun setAttachmentStatus(status: Status): LocalAttachment {
        return LocalAttachment(this, status)
    }

    val isReadyToSend: Boolean
        get() = attachmentStatus == Status.READY_TO_SEND
    val isImage: Boolean
        get() = mimeType?.startsWith("image") ?: false

    enum class Status(val isError: Boolean) {
        UPLOADING(false),
        SECURITY_SCAN(false),
        READY_TO_SEND(false),
        ERROR_NETWORK_TIMEOUT(true),
        ERROR_INTERNAL(true),
        ERROR_INVALID_INPUT(true),
        ERROR_PERMISSIONS_DENIED(true),
        ERROR_FORMAT_UNSUPPORTED(true),
        ERROR_FILE_TOO_LARGE(true),
        ERROR_ENGAGEMENT_MISSING(true),
        ERROR_UNKNOWN(true),
        ERROR_SECURITY_SCAN_FAILED(true),
        ERROR_FILE_UPLOAD_FORBIDDEN(true),
        ERROR_SUPPORTED_FILE_ATTACHMENT_COUNT_EXCEEDED(true)
    }
}
