package com.glia.widgets.chat.domain

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.net.Uri
import android.provider.OpenableColumns
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UriToFileAttachmentUseCaseTest {

    private val uri: Uri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/1")
    private lateinit var contentResolver: ContentResolver
    private lateinit var useCase: UriToFileAttachmentUseCaseImpl

    @Before
    fun setUp() {
        contentResolver = mockk(relaxed = true) {
            every { getType(uri) } returns "video/mp4"
        }
        val context: Context = mockk { every { contentResolver } returns this@UriToFileAttachmentUseCaseTest.contentResolver }
        useCase = UriToFileAttachmentUseCaseImpl(context)
    }

    @Test
    fun `invoke builds an attachment with the given source`() {
        every { contentResolver.query(uri, null, null, null, null) } returns cursorWithRow("clip.mp4", 42L)

        val attachment = useCase(uri, LocalAttachment.Source.MEDIA_PICKER)

        assertEquals(LocalAttachment(uri, "video/mp4", "clip.mp4", 42L, LocalAttachment.Source.MEDIA_PICKER), attachment)
    }

    @Test
    fun `invoke keeps the file browser source`() {
        every { contentResolver.query(uri, null, null, null, null) } returns cursorWithRow("report.pdf", 7L)

        val attachment = useCase(uri, LocalAttachment.Source.FILE_BROWSER)

        assertEquals(LocalAttachment.Source.FILE_BROWSER, attachment?.source)
    }

    @Test
    fun `invoke returns null when the uri has no rows`() {
        every { contentResolver.query(uri, null, null, null, null) } returns
            MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE))

        assertNull(useCase(uri, LocalAttachment.Source.MEDIA_PICKER))
    }

    private fun cursorWithRow(name: String, size: Long): MatrixCursor =
        MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)).apply { addRow(arrayOf<Any>(name, size)) }
}
