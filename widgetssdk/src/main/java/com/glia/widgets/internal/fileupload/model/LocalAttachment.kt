package com.glia.widgets.internal.fileupload.model

import android.net.Uri
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.widgets.chat.model.VisitorAttachmentItem
import java.util.UUID

internal data class LocalAttachment(
    val uri: Uri,
    val mimeType: String?,
    val displayName: String,
    val size: Long,
    val attachmentStatus: Status = Status.UPLOADING,
    val engagementFile: EngagementFile? = null,
) {

    val isReadyToSend: Boolean
        get() = attachmentStatus == Status.READY_TO_SEND
    val isImage: Boolean
        get() = mimeType?.startsWith("image") ?: false
    val id: String
        get() = engagementFile?.id ?: UUID.randomUUID().toString()

    fun toVisitorAttachmentItem(messageId: String): VisitorAttachmentItem = if (isImage) {
        VisitorAttachmentItem.LocalImage(id, messageId, this)
    } else {
        VisitorAttachmentItem.LocalFile(id, messageId, this)
    }

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
