package com.glia.widgets.filepreview.data

import android.graphics.Bitmap
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.di.GliaCore
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache
import com.glia.widgets.filepreview.domain.exception.CacheFileNotFoundException
import com.glia.widgets.helper.fileName
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import java.io.InputStream

internal class GliaFileRepositoryImpl(
    private val bitmapCache: InAppBitmapCache,
    private val downloadsFolderDataSource: DownloadsFolderDataSource,
    private val gliaCore: GliaCore
) : GliaFileRepository {
    private val secureConversations: SecureConversations by lazy {
        gliaCore.secureConversations
    }

    override fun isReadyForPreview(attachmentFile: AttachmentFile): Boolean =
        downloadsFolderDataSource.isDownloaded(attachmentFile) ||
            bitmapCache.getBitmapById(attachmentFile.fileName) != null

    override fun loadImageFromCache(fileName: String): Maybe<Bitmap> = Maybe.create { emitter ->
        bitmapCache.getBitmapById(fileName)?.let {
            emitter.onSuccess(it)
        } ?: emitter.onError(CacheFileNotFoundException())
    }

    override fun putImageToCache(fileName: String, bitmap: Bitmap) {
        bitmapCache.putBitmap(fileName, bitmap)
    }

    override fun loadImageFromDownloads(fileName: String): Maybe<Bitmap> =
        downloadsFolderDataSource.getImageFromDownloadsFolder(fileName)

    override fun putImageToDownloads(fileName: String, bitmap: Bitmap): Completable =
        downloadsFolderDataSource.putImageToDownloads(fileName, bitmap)

    override fun loadImageFileFromNetwork(shouldUseSecureMessagingEndpoints: Boolean, attachmentFile: AttachmentFile): Maybe<InputStream> {
        return fetchFileMaybe(shouldUseSecureMessagingEndpoints, attachmentFile)
    }

    override fun downloadFileFromNetwork(shouldUseSecureMessagingEndpoints: Boolean, attachmentFile: AttachmentFile): Completable {
        return fetchFileMaybe(shouldUseSecureMessagingEndpoints, attachmentFile).flatMapCompletable {
            downloadsFolderDataSource.downloadFileToDownloads(
                attachmentFile.fileName,
                attachmentFile.contentType,
                it
            )
        }
    }

    private fun fetchFileMaybe(shouldUseSecureMessagingEndpoints: Boolean, attachmentFile: AttachmentFile): Maybe<InputStream> =
        Maybe.create { emitter ->
            fetchFile(shouldUseSecureMessagingEndpoints, attachmentFile) { response, exception ->
                when {
                    exception != null -> emitter.onError(exception)
                    response != null -> emitter.onSuccess(response)
                    else -> emitter.onError(RuntimeException("Fetching file failed"))
                }
            }
        }

    private fun fetchFile(shouldUseSecureMessagingEndpoints: Boolean, attachmentFile: AttachmentFile, callback: RequestCallback<InputStream?>) {
        if (shouldUseSecureMessagingEndpoints) {
            secureConversations.fetchFile(attachmentFile.id, callback)
        } else {
            gliaCore.fetchFile(attachmentFile, callback)
        }
    }
}
