package com.glia.widgets.chat.domain

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface

internal interface FixCapturedPictureRotationUseCase {
    operator fun invoke(imageUri: Uri)
}

internal class FixCapturedPictureRotationUseCaseImpl(context: Context) : FixCapturedPictureRotationUseCase {
    private val contentResolver: ContentResolver by lazy { context.contentResolver }
    override fun invoke(imageUri: Uri) {
        val matrix: Matrix = contentResolver.openInputStream(imageUri)?.use {
            val orientation = ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            Matrix().apply { postRotate(rotation) }
        } ?: Matrix()

        val bitmap: Bitmap = contentResolver.openInputStream(imageUri)?.use {
            val rawBitmap: Bitmap = BitmapFactory.decodeStream(it) ?: return

            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } ?: return

        contentResolver.openOutputStream(imageUri)?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    }
}
