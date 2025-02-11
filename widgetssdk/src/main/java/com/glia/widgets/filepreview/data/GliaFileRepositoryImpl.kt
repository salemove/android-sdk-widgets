package com.glia.widgets.filepreview.data

import android.graphics.Bitmap
import com.glia.androidsdk.chat.AttachmentFile
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

    override fun loadImageFileFromNetwork(attachmentFile: AttachmentFile): Maybe<InputStream> {
        return fetchFileMaybe(attachmentFile)
    }

    override fun downloadFileFromNetwork(attachmentFile: AttachmentFile): Completable {
        return fetchFileMaybe(attachmentFile).flatMapCompletable {
            downloadsFolderDataSource.downloadFileToDownloads(
                attachmentFile.fileName,
                attachmentFile.contentType,
                it
            )
        }
    }

    private fun fetchFileMaybe(attachmentFile: AttachmentFile): Maybe<InputStream> = Maybe.create { emitter ->
        gliaCore.fetchFile(attachmentFile) { response, exception ->
            when {
                exception != null -> emitter.onError(exception)
                response != null -> emitter.onSuccess(response)
                else -> emitter.onError(RuntimeException("Fetching file failed"))
            }
        }
    }
}
