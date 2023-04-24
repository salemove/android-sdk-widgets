package com.glia.widgets.filepreview.data

import android.graphics.Bitmap
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.di.GliaCore
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache
import com.glia.widgets.filepreview.domain.exception.CacheFileNotFoundException
import com.glia.widgets.helper.fileName
import io.reactivex.Completable
import io.reactivex.Maybe
import java.io.InputStream

internal class GliaFileRepositoryImpl(
    private val bitmapCache: InAppBitmapCache,
    private val downloadsFolderDataSource: DownloadsFolderDataSource,
    private val gliaCore: GliaCore,
    private val engagementConfigRepository: GliaEngagementConfigRepository
) : GliaFileRepository {
    private val secureConversations: SecureConversations by lazy {
        gliaCore.secureConversations
    }

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
        return fetchFileMaybe(attachmentFile).flatMapCompletable { inputStream ->
            inputStream.use {
                downloadsFolderDataSource.downloadFileToDownloads(
                    attachmentFile.fileName,
                    attachmentFile.contentType,
                    it
                )
            }
        }
    }

    private fun fetchFileMaybe(attachmentFile: AttachmentFile): Maybe<InputStream> =
        Maybe.create { emitter ->
            fetchFile(attachmentFile) { response, exception ->
                exception?.also { emitter.onError(it) } ?: emitter.onSuccess(response)
            }
        }

    private fun fetchFile(attachmentFile: AttachmentFile, callback: RequestCallback<InputStream>) {
        val engagement = gliaCore.currentEngagement.orElse(null)
        if (engagement == null && engagementConfigRepository.chatType == ChatType.SECURE_MESSAGING) {
            secureConversations.fetchFile(attachmentFile.id, callback)
        } else {
            gliaCore.fetchFile(attachmentFile, callback)
        }
    }
}
