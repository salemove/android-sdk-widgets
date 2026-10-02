@file:JvmName("FileHelper")

package com.glia.widgets.helper

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.glia.androidsdk.chat.AttachmentFile
import java.io.File

internal fun String?.toFileExtensionOrEmpty() = this?.let { File(it) }?.extension.orEmpty()

internal val AttachmentFile.fileName: String get() = toFileName(id, name)

internal fun AttachmentFile.isDownloaded(context: Context): Boolean = isDownloaded(context, this)

internal fun isDownloaded(context: Context, attachmentFile: AttachmentFile): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getContentUriApi29(attachmentFile.fileName, context) != null
    } else {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).run {
            File(toString(), attachmentFile.fileName)
        }.exists()
    }

internal fun toFileName(fileId: String?, name: String?): String {
    val fileExtension = name.toFileExtensionOrEmpty()
    if (fileExtension.isEmpty()) {
        return fileId.toString()
    }
    return "$fileId.$fileExtension"
}

internal val Context.fileProviderAuthority: String
    get() = "$packageName.com.glia.widgets.fileprovider"

@RequiresApi(Build.VERSION_CODES.Q)
private fun getContentUriApi29(fileName: String, context: Context): Uri? {
    val downloadsContentUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
    val projection = arrayOf(
        MediaStore.Downloads._ID,
        MediaStore.Downloads.DISPLAY_NAME,
        MediaStore.Downloads.SIZE
    )
    val selection = MediaStore.Downloads.DISPLAY_NAME + " == ?"
    val selectionArgs = arrayOf(fileName)
    val sortOrder = MediaStore.Downloads.DISPLAY_NAME + " ASC"

    return context.contentResolver.query(downloadsContentUri, projection, selection, selectionArgs, sortOrder)?.use {
        if (it.count == 0) {
            return@use null
        }
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Downloads._ID)
        it.moveToFirst()
        val id = it.getLong(idColumn)
        ContentUris.withAppendedId(downloadsContentUri, id)
    }
}

private fun getContentUri(fileName: String, context: Context): Uri =
    File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        fileName
    ).let {
        FileProvider.getUriForFile(context, context.fileProviderAuthority, it)
    }

internal fun getContentUriCompat(fileName: String, context: Context): Uri =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getContentUriApi29(fileName, context) ?: Uri.EMPTY
    } else {
        getContentUri(fileName, context)
    }
