package com.glia.widgets.internal.fileupload.domain

import android.content.Context
import android.net.Uri
import com.glia.widgets.chat.domain.FileProviderUseCase
import com.glia.widgets.helper.isPhotoPickerUri
import com.glia.widgets.helper.toFileExtensionOrEmpty
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileNotFoundException

internal const val SHARED_ATTACHMENTS_DIR = "glia_shared_attachments"
private const val TEMP_FILE_SUFFIX = ".tmp"
// The cached copy is named from the attachment id and the extension of its display name, which comes from
// another app's content provider. Any character outside this set is replaced with "_", so the name can't
// contain a path separator and the copy always lands directly in the shared attachments folder.
private val UNSAFE_FILE_NAME_CHARS: Regex = Regex("[^A-Za-z0-9._-]")

/**
 * Provides a URI for a local attachment that can be granted to another app, e.g. an external viewer or a share target.
 *
 * Photo Picker URIs can only be read by this app, so their content is copied into the app cache on demand
 * and exposed through the SDK FileProvider. Other URIs are returned unchanged.
 */
internal interface GetShareableLocalAttachmentUriUseCase {
    operator fun invoke(attachment: LocalAttachment): Single<Uri>

    /** Same as the [LocalAttachment] variant, for callers that only hold the attachment fields. */
    operator fun invoke(uri: Uri, fileId: String, displayName: String, size: Long): Single<Uri>

    /** Deletes all copies made by this use case. */
    fun clearCache(): Completable
}

internal class GetShareableLocalAttachmentUriUseCaseImpl @JvmOverloads constructor(
    private val context: Context,
    private val fileProviderUseCase: FileProviderUseCase,
    private val ioScheduler: Scheduler = Schedulers.io()
) : GetShareableLocalAttachmentUriUseCase {

    private val cacheDir: File get() = File(context.cacheDir, SHARED_ATTACHMENTS_DIR)

    override fun invoke(attachment: LocalAttachment): Single<Uri> =
        invoke(attachment.uri, attachment.id, attachment.displayName, attachment.size)

    override fun invoke(uri: Uri, fileId: String, displayName: String, size: Long): Single<Uri> = if (uri.isPhotoPickerUri) {
        Single.fromCallable { fileProviderUseCase.getUriForFile(copyToCache(uri, fileId, displayName, size)) }.subscribeOn(ioScheduler)
    } else {
        Single.just(uri)
    }

    override fun clearCache(): Completable = Completable.fromAction { cacheDir.deleteRecursively() }.subscribeOn(ioScheduler)

    private fun copyToCache(uri: Uri, fileId: String, displayName: String, size: Long): File {
        val directory = cacheDir.apply { mkdirs() }
        val target = File(directory, fileNameFor(fileId, displayName))
        if (target.exists() && target.length() == size) return target

        // Copy to a temporary file first, so an interrupted copy is never mistaken for a complete one.
        val temp = File(directory, target.name + TEMP_FILE_SUFFIX)
        try {
            val input = context.contentResolver.openInputStream(uri) ?: throw FileNotFoundException("Can't open attachment input stream")
            input.use { source -> temp.outputStream().use { source.copyTo(it) } }
            if (!temp.renameTo(target)) throw FileNotFoundException("Can't move attachment copy into place")
        } finally {
            temp.delete()
        }
        return target
    }

    private fun fileNameFor(fileId: String, displayName: String): String {
        val baseName = fileId.replace(UNSAFE_FILE_NAME_CHARS, "_")
        val extension = displayName.toFileExtensionOrEmpty().replace(UNSAFE_FILE_NAME_CHARS, "_")
        return if (extension.isEmpty()) baseName else "$baseName.$extension"
    }
}
