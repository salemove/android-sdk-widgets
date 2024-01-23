package com.glia.widgets.snapshotutils

import android.net.Uri
import androidx.core.net.toUri
import com.glia.widgets.core.fileupload.model.FileAttachment
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal interface SnapshotAttachment {

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
