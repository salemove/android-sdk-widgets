package com.glia.widgets.filepreview.data

import android.graphics.Bitmap
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.chat.ChatType
import com.glia.widgets.chat.helper.FileHelper
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.di.GliaCore
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache
import com.glia.widgets.filepreview.domain.exception.CacheFileNotFoundException
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import java.io.InputStream

class GliaFileRepositoryImpl(
    private val bitmapCache: InAppBitmapCache,
    private val downloadsFolderDataSource: DownloadsFolderDataSource,
    private val gliaCore: GliaCore,
    private val fileHelper: FileHelper,
    private val engagementConfigRepository: GliaEngagementConfigRepository
) : GliaFileRepository {
    private val secureConversations: SecureConversations by lazy {
        gliaCore.secureConversations
    }

    override fun loadImageFromCache(fileName: String): Maybe<Bitmap> {
        return Maybe.create { emitter: MaybeEmitter<Bitmap> ->
            val bitmap = bitmapCache.getBitmapById(fileName)
            if (bitmap != null) emitter.onSuccess(bitmap) else emitter.onError(
                CacheFileNotFoundException()
            )
        }
    }

    override fun putImageToCache(fileName: String, bitmap: Bitmap): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            try {
                bitmapCache.putBitmap(fileName, bitmap)
                emitter.onComplete()
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    override fun loadImageFromDownloads(fileName: String): Maybe<Bitmap> {
        return downloadsFolderDataSource.getImageFromDownloadsFolder(fileName)
    }

    override fun putImageToDownloads(fileName: String, bitmap: Bitmap): Completable {
        return downloadsFolderDataSource.putImageToDownloads(fileName, bitmap)
    }

    override fun loadImageFileFromNetwork(attachmentFile: AttachmentFile): Maybe<Bitmap> {
        return Maybe.create { emitter: MaybeEmitter<InputStream?> ->
            fetchFile(attachmentFile) { fileInputStream: InputStream, gliaException: GliaException? ->
                if (gliaException != null) {
                    emitter.onError(gliaException)
                } else {
                    emitter.onSuccess(fileInputStream)
                }
            }
        }
            .flatMap { inputStream: InputStream? ->
                fileHelper.decodeSampledBitmapFromInputStream(inputStream)
            }
    }

    override fun downloadFileFromNetwork(attachmentFile: AttachmentFile): Completable {
        return Maybe.create { emitter: MaybeEmitter<InputStream> ->
            fetchFile(attachmentFile) { fileInputStream: InputStream, gliaException: GliaException? ->
                if (gliaException != null) {
                    emitter.onError(gliaException)
                } else {
                    emitter.onSuccess(fileInputStream)
                }
            }
        }
            .flatMapCompletable { inputStream: InputStream ->
                downloadsFolderDataSource
                    .downloadFileToDownloads(
                        FileHelper.getFileName(attachmentFile),
                        attachmentFile.contentType,
                        inputStream
                    )
                    .doOnComplete { inputStream.close() }
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
