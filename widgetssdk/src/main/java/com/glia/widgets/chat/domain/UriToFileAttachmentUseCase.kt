package com.glia.widgets.chat.domain

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.glia.widgets.core.fileupload.model.FileAttachment

internal interface UriToFileAttachmentUseCase {
    operator fun invoke(uri: Uri): FileAttachment?
}

internal class UriToFileAttachmentUseCaseImpl(context: Context) : UriToFileAttachmentUseCase {
    private val contentResolver: ContentResolver by lazy { context.contentResolver }
    override fun invoke(uri: Uri): FileAttachment? = contentResolver.query(uri, null, null, null, null)?.use {
        if (it.count == 0) return@use null

        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
        it.moveToFirst()
        val displayName = it.getString(nameIndex)
        val mimeType = contentResolver.getType(uri)
        val size = it.getLong(sizeIndex)
        FileAttachment(uri, mimeType, displayName, size)
    }
}
