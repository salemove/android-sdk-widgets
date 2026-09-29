package com.glia.widgets.internal.fileupload.domain

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.glia.widgets.chat.domain.FileProviderUseCase
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException

@RunWith(RobolectricTestRunner::class)
class GetShareableLocalAttachmentUriUseCaseTest {

    private lateinit var context: Context
    private lateinit var fileProviderUseCase: FileProviderUseCase
    private lateinit var useCase: GetShareableLocalAttachmentUriUseCaseImpl

    private val sharedDir: File get() = File(context.cacheDir, SHARED_ATTACHMENTS_DIR)
    private val providerUri: Uri = Uri.parse("content://com.glia.test.fileprovider/shared_attachments/file")
    private val copiedFile = slot<File>()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fileProviderUseCase = mockk {
            every { getUriForFile(capture(copiedFile)) } returns providerUri
        }
        useCase = GetShareableLocalAttachmentUriUseCaseImpl(context, fileProviderUseCase, Schedulers.trampoline())
    }

    @After
    fun tearDown() {
        sharedDir.deleteRecursively()
    }

    @Test
    fun `document provider uri is returned unchanged`() {
        val uri = Uri.parse("content://com.android.providers.media.documents/document/video%3A1")

        useCase(attachment(uri)).test().assertValue(uri)

        verify(exactly = 0) { fileProviderUseCase.getUriForFile(any()) }
        assertFalse(sharedDir.exists())
    }

    @Test
    fun `file provider uri is returned unchanged`() {
        val uri = Uri.parse("content://com.glia.test.fileprovider/files/IMG_1.jpg")

        useCase(attachment(uri)).test().assertValue(uri)

        verify(exactly = 0) { fileProviderUseCase.getUriForFile(any()) }
    }

    @Test
    fun `photo picker uri is copied to cache and exposed through file provider`() {
        val uri = PICKER_URI
        registerContent(uri, CONTENT)

        useCase(attachment(uri)).test().assertValue(providerUri)

        val file = copiedFile.captured
        assertEquals(sharedDir, file.parentFile)
        assertEquals("file-id.mp4", file.name)
        assertArrayEquals(CONTENT, file.readBytes())
        assertFalse(File(sharedDir, "file-id.mp4.tmp").exists())
    }

    @Test
    fun `existing complete copy is reused`() {
        val uri = PICKER_URI
        registerContent(uri, CONTENT)
        useCase(attachment(uri)).test().assertComplete()

        // Replace the source content: a reused copy keeps the original bytes.
        registerContent(uri, "changed".toByteArray())
        useCase(attachment(uri, size = CONTENT.size.toLong())).test().assertValue(providerUri)

        assertArrayEquals(CONTENT, copiedFile.captured.readBytes())
    }

    @Test
    fun `incomplete copy is replaced`() {
        val uri = PICKER_URI
        sharedDir.mkdirs()
        File(sharedDir, "file-id.mp4").writeBytes(byteArrayOf(1))
        registerContent(uri, CONTENT)

        useCase(attachment(uri)).test().assertValue(providerUri)

        assertArrayEquals(CONTENT, copiedFile.captured.readBytes())
    }

    @Test
    fun `error is emitted when picker content can not be opened`() {
        val uri = PICKER_URI
        shadowOf(context.contentResolver).registerInputStreamSupplier(uri) { throw FileNotFoundException("gone") }

        useCase(attachment(uri)).test().assertError(FileNotFoundException::class.java)

        verify(exactly = 0) { fileProviderUseCase.getUriForFile(any()) }
    }

    @Test
    fun `unsafe characters are removed from the cached file name`() {
        val uri = PICKER_URI
        registerContent(uri, CONTENT)

        useCase(attachment(uri, id = "../evil id", displayName = "clip.m/p4")).test().assertComplete()

        assertEquals(sharedDir, copiedFile.captured.parentFile)
        assertTrue(copiedFile.captured.name.none { it == '/' || it == ' ' })
    }

    @Test
    fun `clearCache deletes all copies`() {
        registerContent(PICKER_URI, CONTENT)
        useCase(attachment(PICKER_URI)).test().assertComplete()
        assertTrue(sharedDir.exists())

        useCase.clearCache().test().assertComplete()

        assertFalse(sharedDir.exists())
    }

    private fun registerContent(uri: Uri, bytes: ByteArray) {
        shadowOf(context.contentResolver).registerInputStreamSupplier(uri) { ByteArrayInputStream(bytes) }
    }

    private fun attachment(
        uri: Uri,
        id: String = "file-id",
        displayName: String = "clip.mp4",
        size: Long = CONTENT.size.toLong()
    ): LocalAttachment = LocalAttachment(
        uri = uri,
        mimeType = "video/mp4",
        displayName = displayName,
        size = size,
        engagementFile = mockk { every { this@mockk.id } returns id }
    )

    private companion object {
        val PICKER_URI: Uri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/1000020228")
        val CONTENT: ByteArray = "video-bytes".toByteArray()
    }
}
