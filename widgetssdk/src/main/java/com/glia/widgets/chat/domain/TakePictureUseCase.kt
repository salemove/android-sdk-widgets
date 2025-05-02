package com.glia.widgets.chat.domain

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.helper.createTempFileCompat
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val FILE_SUFFIX = ".jpg"
private const val PATTERN = "yyyyMMdd_HHmmss_"

internal interface TakePictureUseCase {
    fun prepare(onFileCreated: (Uri) -> Unit)

    fun onImageCaptured(captured: Boolean, onFileReady: (LocalAttachment) -> Unit)
    fun deleteCurrent()
    fun clearUriReference()
}

internal class TakePictureUseCaseImpl(
    private val context: Context,
    private val fileProviderUseCase: FileProviderUseCase,
    private val uriToFileAttachmentUseCase: UriToFileAttachmentUseCase,
    private val fixCapturedPictureRotationUseCase: FixCapturedPictureRotationUseCase
) : TakePictureUseCase {
    private val contentResolver: ContentResolver by lazy { context.contentResolver }
    private val dateFormat: DateFormat by lazy { SimpleDateFormat(PATTERN, Locale.getDefault()) }
    private val fileName: String get() = "IMG_${dateFormat.format(Date())}"

    private var uri: Uri? = null

    override fun prepare(onFileCreated: (Uri) -> Unit) {
        uri = fileProviderUseCase.getUriForFile(createTempPhotoFile()).also(onFileCreated)
    }

    override fun onImageCaptured(captured: Boolean, onFileReady: (LocalAttachment) -> Unit) {
        if (!captured) {
            deleteCurrent()
            return
        }

        fixCapturedPictureRotationUseCase(uri ?: return)
        uriToFileAttachmentUseCase(uri ?: return)?.also(onFileReady)
    }

    override fun deleteCurrent() {
        uri?.also {
            contentResolver.delete(it, null, null)
            uri = null
        }
    }

    override fun clearUriReference() {
        uri = null
    }

    private fun createTempPhotoFile(): File = createTempFileCompat(fileName, FILE_SUFFIX, context.filesDir).apply { deleteOnExit() }

}
