package com.glia.widgets.filepreview.data.source.local

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.getContentUriCompat
import com.glia.widgets.helper.isDownloaded
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream

private val TAG = DownloadsFolderDataSource::class.java.simpleName

/**
 * @hide
 */
internal class DownloadsFolderDataSource(private val context: Context) {

    fun isDownloaded(attachmentFile: AttachmentFile): Boolean {
        return attachmentFile.isDownloaded(context)
    }

    fun getImageFromDownloadsFolder(imageName: String?): Maybe<Bitmap> {
        if (imageName == null) return Maybe.error(NullPointerException("Image name cannot be null"))

        return Maybe.create { emitter ->
            val uri = getContentUriCompat(imageName, context)
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            if (bitmap == null) {
                emitter.onError(FileNotFoundException())
            } else {
                emitter.onSuccess(bitmap)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun putImageToDownloadsAPI29(imageName: String, bitmap: Bitmap): Completable {
        return Completable.create { emitter ->
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                val imageUri = resolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
                val fos = resolver.openOutputStream(requireNotNull(imageUri)) ?: throw FileNotFoundException()
                fos.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
                emitter.onComplete()
            } catch (ex: FileNotFoundException) {
                Logger.e(TAG, "Image saving to downloads folder failed: " + ex.message)
                emitter.onError(ex)
            }
        }
    }

    private fun putImageToDownloadsOld(imageName: String, bitmap: Bitmap): Completable {
        return Completable.create { emitter ->
            try {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                val image = File(imagesDir, imageName)
                FileOutputStream(image).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
                emitter.onComplete()
            } catch (ex: FileNotFoundException) {
                Logger.e(TAG, "Image saving to downloads folder failed: " + ex.message)
                emitter.onError(ex)
            }
        }
    }

    fun putImageToDownloads(fileName: String?, bitmap: Bitmap?): Completable {
        if (fileName == null) return Completable.error(NullPointerException("File name cannot be null"))
        if (bitmap == null) return Completable.error(NullPointerException("Bitmap cannot be null"))

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            putImageToDownloadsAPI29(fileName, bitmap)
        } else {
            putImageToDownloadsOld(fileName, bitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun downloadFileToDownloadsAPI29(fileName: String, contentType: String?, inputStream: InputStream): Completable {
        return Completable.create { emitter ->
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, contentType)
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                val fileUri = resolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
                val fos = resolver.openOutputStream(requireNotNull(fileUri)) ?: throw FileNotFoundException()
                fos.use {
                    val buffer = ByteArray(10 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        it.write(buffer, 0, read)
                    }
                    it.flush()
                }
                emitter.onComplete()
            } catch (ex: FileNotFoundException) {
                Logger.e(TAG, "File saving to downloads folder failed: " + ex.message)
                emitter.onError(ex)
            }
        }
    }

    private fun downloadFileToDownloadsOld(fileName: String, inputStream: InputStream): Completable {
        return Completable.create { emitter ->
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
            val file = File(imagesDir, fileName)
            try {
                FileOutputStream(file).use { fos ->
                    val buffer = ByteArray(10 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        fos.write(buffer, 0, read)
                    }
                    emitter.onComplete()
                }
            } catch (ex: FileNotFoundException) {
                Logger.e(TAG, "File saving to downloads folder failed: " + ex.message)
                emitter.onError(ex)
            }
            emitter.onComplete()
        }
    }

    fun downloadFileToDownloads(fileName: String?, contentType: String?, inputStream: InputStream?): Completable {
        if (fileName == null) return Completable.error(NullPointerException("File name cannot be null"))
        if (inputStream == null) return Completable.error(NullPointerException("InputStream cannot be null"))

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadFileToDownloadsAPI29(fileName, contentType, inputStream)
        } else {
            downloadFileToDownloadsOld(fileName, inputStream)
        }
    }
}
