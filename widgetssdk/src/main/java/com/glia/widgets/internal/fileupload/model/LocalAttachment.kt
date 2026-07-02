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

    /**
     * Maps this attachment to its optimistic chat item, or `null` when the file has not been
     * uploaded yet. The uploaded file id is used as both the item id and the message id because
     * each file is sent as a separate message and the file id is the only key available to
     * reconcile it with the echoed message from the incoming message stream.
     */
    fun toVisitorAttachmentItem(): VisitorAttachmentItem? = when {
        engagementFile == null -> null
        isImage -> VisitorAttachmentItem.LocalImage(engagementFile.id, engagementFile.id, this)
        else -> VisitorAttachmentItem.LocalFile(engagementFile.id, engagementFile.id, this)
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
