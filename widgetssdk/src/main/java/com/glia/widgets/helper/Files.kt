@file:JvmName("FileHelper")

package com.glia.widgets.helper

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.core.fileupload.model.FileAttachment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun String?.toFileExtensionOrEmpty() = this?.let { File(it) }?.extension.orEmpty()

internal val AttachmentFile.fileName: String
    get() = toFileName(id, name)

internal val AttachmentFile.isDownloaded: Boolean
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).run {
        File(path, fileName)
    }.exists()

internal fun toFileName(fileId: String?, name: String?): String {
    val fileExtension = name.toFileExtensionOrEmpty()
    if (fileExtension.isEmpty()) {
        return fileId.toString()
    }
    return "$fileId.$fileExtension"
}

internal val Context.fileProviderAuthority: String
    get() = "$packageName.com.glia.widgets.fileprovider"

internal fun fixCapturedPhotoRotation(uri: Uri, context: Context) {
    with(context.contentResolver) {
        val matrix: Matrix = openInputStream(uri)?.use {
            val orientation = ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            Matrix().apply { postRotate(rotation) }
        } ?: Matrix()

        val bitmap: Bitmap = openInputStream(uri)?.use {
            val rawBitmap: Bitmap = BitmapFactory.decodeStream(it) ?: return@with

            Bitmap.createBitmap(
                rawBitmap,
                0,
                0,
                rawBitmap.width,
                rawBitmap.height,
                matrix,
                true
            )
        } ?: return@with

        openOutputStream(uri)?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }
}

internal fun getContentUriCompat(fileName: String, context: Context): Uri =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val downloadsContentUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.SIZE
        )
        val selection = MediaStore.Downloads.DISPLAY_NAME + " == ?"
        val selectionArgs = arrayOf(fileName)
        val sortOrder = MediaStore.Downloads.DISPLAY_NAME + " ASC"

        context.contentResolver.query(
            downloadsContentUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use {
            if (it.count == 0) return@use Uri.EMPTY

            val idColumn = it.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            it.moveToFirst()
            val id = it.getLong(idColumn)
            ContentUris.withAppendedId(downloadsContentUri, id)
        } ?: Uri.EMPTY

    } else {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        ).let {
            FileProvider.getUriForFile(context, context.fileProviderAuthority, it)
        }
    }

@Throws(IOException::class)
fun createTempPhotoFile(context: Context): File {
    val directoryStorage = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    directoryStorage?.deleteOnExit()
    return File.createTempFile(generatePhotoFileName(), ".jpg", directoryStorage)
}

private fun generatePhotoFileName(): String =
    SimpleDateFormat("yyyyMMdd_HHmmss_", Locale.getDefault()).run {
        "IMG_${format(Date())}"
    }

fun mapUriToFileAttachment(contentResolver: ContentResolver, uri: Uri): FileAttachment? =
    contentResolver.query(uri, null, null, null, null)?.use {
        if (it.count == 0) return@use null

        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
        it.moveToFirst()
        val displayName = it.getString(nameIndex)
        val mimeType = contentResolver.getType(uri)
        val size = it.getLong(sizeIndex)
        FileAttachment(uri, displayName, size, mimeType)
    }