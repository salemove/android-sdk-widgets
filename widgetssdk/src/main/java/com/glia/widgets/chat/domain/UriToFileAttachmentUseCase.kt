package com.glia.widgets.chat.domain

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.glia.widgets.internal.fileupload.model.LocalAttachment

internal interface UriToFileAttachmentUseCase {
    operator fun invoke(uri: Uri): LocalAttachment?
}

internal class UriToFileAttachmentUseCaseImpl(context: Context) : UriToFileAttachmentUseCase {
    private val contentResolver: ContentResolver by lazy { context.contentResolver }
    override fun invoke(uri: Uri): LocalAttachment? = contentResolver.run {
        tryToObtainPersistablePermission(uri)
        query(uri, null, null, null, null)?.use {
            if (it.count == 0) return@use null

            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            it.moveToFirst()
            val displayName = it.getString(nameIndex)
            val mimeType = contentResolver.getType(uri)
            val size = it.getLong(sizeIndex)
            LocalAttachment(uri, mimeType, displayName, size)
        }
    }

    private fun tryToObtainPersistablePermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: Exception) {
            // Do nothing
        }
    }
}
