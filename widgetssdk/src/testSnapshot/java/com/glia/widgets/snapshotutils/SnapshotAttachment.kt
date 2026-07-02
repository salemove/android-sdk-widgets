package com.glia.widgets.snapshotutils

import android.net.Uri
import androidx.core.net.toUri
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal interface SnapshotAttachment {

    fun remoteAttachment(
        fileId: String = "imageId",
        fileSize: Long = 12345,
        fileContentType: String = "image",
        fileIsDeleted: Boolean = false,
        fileName: String = "tricky_plan.jpg"
    ): AttachmentFile = object : AttachmentFile {
        override val id: String = fileId
        override val size: Long = fileSize
        override val contentType: String = fileContentType
        override val isDeleted: Boolean = fileIsDeleted
        override val name: String = fileName
    }

    fun visitorAttachmentItemImage(attachment: AttachmentFile = remoteAttachment()): VisitorAttachmentItem.RemoteImage {
        return VisitorAttachmentItem.RemoteImage(id = "id", attachment = attachment)
    }

    fun visitorAttachmentItemFile(
        attachment: AttachmentFile = remoteAttachment(
            fileId = "fileId",
            fileSize = 1234567890,
            fileContentType = "pdf",
            fileName = "File Name.pdf"
        )
    ): VisitorAttachmentItem.RemoteFile {
        return VisitorAttachmentItem.RemoteFile(
            id = "fileId",
            attachment = attachment,
            isFileExists = true,
            isDownloading = false
        )
    }

    fun operatorAttachmentItemImage(
        attachment: AttachmentFile = remoteAttachment(),
        id: String = "operatorImageId",
        timestamp: Long = 1706534848,
        showChatHead: Boolean = false,
        operatorProfileImgUrl: String? = null,
        operatorId: String? = "operatorId"
    ) = OperatorAttachmentItem.Image(
        attachment, id, timestamp, showChatHead, operatorProfileImgUrl, operatorId
    )

    fun operatorAttachmentItemFile(
        isFileExists: Boolean = false,
        isDownloading: Boolean = false,
        attachment: AttachmentFile = remoteAttachment(fileId = "pdfId", fileContentType = "pdf", fileName = "Document.pdf"),
        id: String = "operatorImageId",
        timestamp: Long = 1706534848,
        showChatHead: Boolean = false,
        operatorProfileImgUrl: String? = null,
        operatorId: String? = "operatorId"
    ) = OperatorAttachmentItem.File(
        isFileExists, isDownloading, attachment, id, timestamp, showChatHead, operatorProfileImgUrl, operatorId
    )

    fun fileAttachment(
        uri: Uri = "file:///test".toUri(),
        status: LocalAttachment.Status = LocalAttachment.Status.UPLOADING,
        isReadyToSend: Boolean = false,
        displayName: String = "Snapshot.pdf",
        size: Long = 1234,
        isImage: Boolean = false
    ) = mock<LocalAttachment>().also {
        whenever(it.uri).thenReturn(uri)
        whenever(it.attachmentStatus).thenReturn(status)
        whenever(it.isReadyToSend).thenReturn(isReadyToSend)
        whenever(it.displayName).thenReturn(displayName)
        whenever(it.size).thenReturn(size)
        whenever(it.isImage).thenReturn(isImage)
    }
}
