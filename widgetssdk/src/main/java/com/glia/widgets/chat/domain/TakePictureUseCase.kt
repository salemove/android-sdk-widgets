package com.glia.widgets.chat.domain

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.glia.widgets.core.fileupload.model.LocalAttachment
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

    private fun createTempPhotoFile(): File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).let {
        it?.deleteOnExit()
        File.createTempFile(fileName, FILE_SUFFIX, it)
    }

}
