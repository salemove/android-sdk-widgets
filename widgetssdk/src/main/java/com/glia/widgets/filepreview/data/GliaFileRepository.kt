package com.glia.widgets.filepreview.data

import android.graphics.Bitmap
import com.glia.androidsdk.chat.AttachmentFile
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import java.io.InputStream

internal interface GliaFileRepository {
    fun isReadyForPreview(attachmentFile: AttachmentFile): Boolean
    fun loadImageFromCache(fileName: String): Maybe<Bitmap>
    fun putImageToCache(fileName: String, bitmap: Bitmap)
    fun loadImageFromDownloads(fileName: String): Maybe<Bitmap>
    fun putImageToDownloads(fileName: String, bitmap: Bitmap): Completable
    fun loadImageFileFromNetwork(attachmentFile: AttachmentFile): Maybe<InputStream>
    fun downloadFileFromNetwork(attachmentFile: AttachmentFile): Completable
}
