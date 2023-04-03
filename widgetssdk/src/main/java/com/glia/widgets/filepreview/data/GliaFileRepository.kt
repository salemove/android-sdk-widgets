package com.glia.widgets.filepreview.data

import android.graphics.Bitmap
import com.glia.androidsdk.chat.AttachmentFile
import io.reactivex.Completable
import io.reactivex.Maybe

interface GliaFileRepository {
    fun loadImageFromCache(fileName: String): Maybe<Bitmap>
    fun putImageToCache(fileName: String, bitmap: Bitmap): Completable
    fun loadImageFromDownloads(fileName: String): Maybe<Bitmap>
    fun putImageToDownloads(fileName: String, bitmap: Bitmap): Completable
    fun loadImageFileFromNetwork(attachmentFile: AttachmentFile): Maybe<Bitmap>
    fun downloadFileFromNetwork(attachmentFile: AttachmentFile): Completable
}
