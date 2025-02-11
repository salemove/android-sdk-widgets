package com.glia.widgets.filepreview.data.source.local

import android.FILE_HELPER_EXTENSIONS_CLASS_PATH
import android.graphics.Bitmap
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.di.GliaCore
import com.glia.widgets.filepreview.data.GliaFileRepository
import com.glia.widgets.filepreview.data.GliaFileRepositoryImpl
import com.glia.widgets.filepreview.domain.exception.CacheFileNotFoundException
import com.glia.widgets.helper.fileName
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import org.junit.Before
import org.junit.Test
import java.io.InputStream

class GliaFileRepositoryTest {

    private lateinit var bitmapCache: InAppBitmapCache
    private lateinit var downloadsFolderDataSource: DownloadsFolderDataSource
    private lateinit var gliaCore: GliaCore
    private lateinit var secureConversations: SecureConversations
    private lateinit var repository: GliaFileRepository

    @Before
    fun setUp() {
        bitmapCache = mockk()
        downloadsFolderDataSource = mockk()
        gliaCore = mockk(relaxUnitFun = true)
        secureConversations = mockk(relaxUnitFun = true)
        every { gliaCore.secureConversations } returns secureConversations
        repository = GliaFileRepositoryImpl(bitmapCache, downloadsFolderDataSource, gliaCore)
    }

    @Test
    fun `isReadyForPreview returns true when file is downloaded`() {
        val attachmentFile = mockk<AttachmentFile>()
        every { downloadsFolderDataSource.isDownloaded(attachmentFile) } returns true

        val result = repository.isReadyForPreview(attachmentFile)

        assert(result)
        verify { downloadsFolderDataSource.isDownloaded(attachmentFile) }
    }

    @Test
    fun `isReadyForPreview returns true when bitmap is in cache`() {
        mockkStatic(FILE_HELPER_EXTENSIONS_CLASS_PATH)
        val attachmentFile = mockk<AttachmentFile>()
        val fileName = "fileName"
        every { any<AttachmentFile>().fileName } returns fileName
        every { bitmapCache.getBitmapById(fileName) } returns mockk()
        every { downloadsFolderDataSource.isDownloaded(attachmentFile) } returns false

        val result = repository.isReadyForPreview(attachmentFile)

        assert(result)
        verify { bitmapCache.getBitmapById(fileName) }
        unmockkStatic(FILE_HELPER_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `loadImageFromCache returns bitmap when found`() {
        val fileName = "fileName"
        val bitmap = mockk<Bitmap>()
        every { bitmapCache.getBitmapById(fileName) } returns bitmap

        val testObserver = repository.loadImageFromCache(fileName).test()

        testObserver.assertValue(bitmap)
        testObserver.assertComplete()
        verify { bitmapCache.getBitmapById(fileName) }
    }

    @Test
    fun `loadImageFromCache returns error when not found`() {
        val fileName = "fileName"
        every { bitmapCache.getBitmapById(fileName) } returns null

        val testObserver = repository.loadImageFromCache(fileName).test()

        testObserver.assertError(CacheFileNotFoundException::class.java)
        verify { bitmapCache.getBitmapById(fileName) }
    }

    @Test
    fun `putImageToCache stores bitmap in cache`() {
        val fileName = "fileName"
        val bitmap = mockk<Bitmap>()
        every { bitmapCache.putBitmap(fileName, bitmap) } just Runs

        repository.putImageToCache(fileName, bitmap)

        verify { bitmapCache.putBitmap(fileName, bitmap) }
    }

    @Test
    fun `loadImageFromDownloads returns bitmap when found`() {
        val fileName = "fileName"
        val bitmap = mockk<Bitmap>()
        every { downloadsFolderDataSource.getImageFromDownloadsFolder(fileName) } returns Maybe.just(bitmap)

        val testObserver = repository.loadImageFromDownloads(fileName).test()

        testObserver.assertValue(bitmap)
        testObserver.assertComplete()
        verify { downloadsFolderDataSource.getImageFromDownloadsFolder(fileName) }
    }

    @Test
    fun `putImageToDownloads stores bitmap in downloads folder`() {
        val fileName = "fileName"
        val bitmap = mockk<Bitmap>()
        every { downloadsFolderDataSource.putImageToDownloads(fileName, bitmap) } returns Completable.complete()

        val testObserver = repository.putImageToDownloads(fileName, bitmap).test()

        testObserver.assertComplete()
        verify { downloadsFolderDataSource.putImageToDownloads(fileName, bitmap) }
    }

    @Test
    fun `loadImageFileFromNetwork fetches file from network`() {
        val requestCallbackSlot = slot<RequestCallback<InputStream?>>()
        val attachmentFile = mockk<AttachmentFile>()
        val inputStream = mockk<InputStream>()

        val testObserver = repository.loadImageFileFromNetwork(attachmentFile).test()

        verify { gliaCore.fetchFile(attachmentFile, capture(requestCallbackSlot)) }
        requestCallbackSlot.captured.onResult(inputStream, null)

        testObserver.awaitCount(1).assertValue(inputStream).assertComplete()
    }

    @Test
    fun `loadImageFileFromNetwork returns error when failure`() {
        val requestCallbackSlot = slot<RequestCallback<InputStream?>>()
        val attachmentFile = mockk<AttachmentFile>()
        val testObserver = repository.loadImageFileFromNetwork(attachmentFile).test()

        verify { gliaCore.fetchFile(attachmentFile, capture(requestCallbackSlot)) }
        val exception = GliaException("error", GliaException.Cause.AUTHENTICATION_ERROR)
        requestCallbackSlot.captured.onResult(null, exception)

        testObserver.awaitCount(1).assertError(exception)
    }

    @Test
    fun `loadImageFileFromNetwork returns error when response and exception are null`() {
        val requestCallbackSlot = slot<RequestCallback<InputStream?>>()
        val attachmentFile = mockk<AttachmentFile>()
        val testObserver = repository.loadImageFileFromNetwork(attachmentFile).test()

        verify { gliaCore.fetchFile(attachmentFile, capture(requestCallbackSlot)) }
        requestCallbackSlot.captured.onResult(null, null)

        testObserver.awaitCount(1).assertError { it is RuntimeException }
    }

    @Test
    fun `downloadFileFromNetwork downloads file to downloads folder`() {
        mockkStatic(FILE_HELPER_EXTENSIONS_CLASS_PATH)
        val requestCallbackSlot = slot<RequestCallback<InputStream?>>()

        val attachmentFile = mockk<AttachmentFile>()
        every { attachmentFile.contentType } returns "contentType"
        val fileName = "fileName"
        every { any<AttachmentFile>().fileName } returns fileName
        val inputStream = mockk<InputStream>()
        every { downloadsFolderDataSource.downloadFileToDownloads(any(), any(), any()) } returns Completable.complete()

        val testObserver = repository.downloadFileFromNetwork(attachmentFile).test()
        verify { gliaCore.fetchFile(attachmentFile, capture(requestCallbackSlot)) }
        requestCallbackSlot.captured.onResult(inputStream, null)

        testObserver.assertComplete()
        verify { downloadsFolderDataSource.downloadFileToDownloads(eq("fileName"), eq("contentType"), inputStream) }
        unmockkStatic(FILE_HELPER_EXTENSIONS_CLASS_PATH)
    }
}
