package com.glia.widgets.snapshotutils

import android.net.Uri
import androidx.core.net.toUri
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.chat.model.Attachment
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.core.fileupload.model.FileAttachment
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal interface SnapshotAttachment {

    fun remoteAttachment(
        id: String = "imageId",
        size: Long = 12345,
        contentType: String = "image",
        isDeleted: Boolean = false,
        name: String ="tricky_plan.jpg"
    ) = Attachment.Remote(
        object : AttachmentFile {
            override fun getId(): String = id
            override fun getSize(): Long = size
            override fun getContentType(): String = contentType
            override fun isDeleted(): Boolean = isDeleted
            override fun getName(): String = name
        }
    )

    fun visitorAttachmentItemImage(
        attachment: Attachment = remoteAttachment(),
        showDelivered: Boolean = false,
        showError: Boolean = false
    ) = VisitorAttachmentItem.Image(
        id = "id",
        attachment = attachment,
        showDelivered = showDelivered,
        showError = showError
    )

    fun visitorAttachmentItemFile(
        attachment: Attachment = remoteAttachment(
            id = "fileId",
            size = 1234567890,
            contentType = "pdf",
            name = "File Name.pdf"
        ),
        showDelivered: Boolean = false,
        showError: Boolean = false
    ) = VisitorAttachmentItem.File(
        id = "fileId",
        attachment = attachment,
        showDelivered = showDelivered,
        showError = showError
    )

    fun fileAttachment(
        uri: Uri = "file:///test".toUri(),
        status: FileAttachment.Status = FileAttachment.Status.UPLOADING,
        isReadyToSend: Boolean = false,
        displayName: String = "Snapshot.pdf",
        size: Long = 1234,
        isImage: Boolean = false
    ) = mock<FileAttachment>().also {
        whenever(it.uri).thenReturn(uri)
        whenever(it.attachmentStatus).thenReturn(status)
        whenever(it.isReadyToSend).thenReturn(isReadyToSend)
        whenever(it.displayName).thenReturn(displayName)
        whenever(it.size).thenReturn(size)
        whenever(it.isImage).thenReturn(isImage)
    }
}
