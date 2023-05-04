@file:JvmName("FileHelper")

package com.glia.widgets.helper

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.glia.androidsdk.chat.AttachmentFile
import java.io.File

internal fun String?.toFileExtensionOrEmpty() = this?.let { File(it) }?.extension.orEmpty()

internal val AttachmentFile.fileName: String
    get() = id + name.toFileExtensionOrEmpty()

internal val AttachmentFile.isDownloaded: Boolean
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).run {
        File(path, fileName)
    }.exists()

internal fun toFileName(fileId: String?, name: String?): String =
    fileId + name.toFileExtensionOrEmpty()

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
                val rawBitmap = BitmapFactory.decodeStream(it)

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
        )
            ?.use {
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