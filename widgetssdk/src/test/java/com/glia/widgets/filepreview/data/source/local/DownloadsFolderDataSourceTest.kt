package com.glia.widgets.filepreview.data.source.local

import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBitmapFactory
import org.robolectric.shadows.ShadowEnvironment
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
internal class DownloadsFolderDataSourceTest {

    private lateinit var dataSource: DownloadsFolderDataSource

    @Before
    fun setUp() {
        dataSource = DownloadsFolderDataSource(ApplicationProvider.getApplicationContext())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Robolectric.setupContentProvider(FakeMediaDownloadsProvider::class.java, MediaStore.AUTHORITY)
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs()
        }
    }

    // Null-argument contracts (SDK-independent, checked before any storage branch)

    @Test
    fun putImageToDownloads_emitsNullPointerException_whenNullImageNameArgument() {
        dataSource.putImageToDownloads(null, BITMAP)
            .test()
            .assertError(NullPointerException::class.java)
    }

    @Test
    fun putImageToDownloads_emitsNullPointerException_whenNullArguments() {
        dataSource.putImageToDownloads(null, null)
            .test()
            .assertError(NullPointerException::class.java)
    }

    @Test
    fun getImageFromDownloadsFolder_emitsNullPointerException_whenNullArgument() {
        dataSource.getImageFromDownloadsFolder(null)
            .test()
            .assertError(NullPointerException::class.java)
    }

    @Test
    fun downloadFileToDownloads_emitsNullPointerException_whenNullFileNameArgument() {
        dataSource.downloadFileToDownloads(null, CONTENT_TYPE, inputStream())
            .test()
            .assertError(NullPointerException::class.java)
    }

    @Test
    fun putImageToDownloads_emitsNullPointerException_whenNullBitmapArgument() {
        dataSource.putImageToDownloads(IMAGE_NAME, null)
            .test()
            .assertError(NullPointerException::class.java)
    }

    @Test
    fun downloadFileToDownloads_emitsNullPointerException_whenNullInputStreamArgument() {
        dataSource.downloadFileToDownloads(FILE_NAME, CONTENT_TYPE, null)
            .test()
            .assertError(NullPointerException::class.java)
    }

    // Legacy external-storage path (API < 29)

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun putImageToDownloads_completesSuccessfully_onLegacyStorage() {
        dataSource.putImageToDownloads(IMAGE_NAME, BITMAP)
            .test()
            .assertComplete()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun downloadFileToDownloads_completesSuccessfully_onLegacyStorage() {
        dataSource.downloadFileToDownloads(FILE_NAME, CONTENT_TYPE, inputStream())
            .test()
            .assertComplete()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun downloadFileToDownloads_completesSuccessfully_whenNullContentType_onLegacyStorage() {
        dataSource.downloadFileToDownloads(FILE_NAME, null, inputStream())
            .test()
            .assertComplete()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun downloadFileToDownloads_writesFileContent_onLegacyStorage() {
        dataSource.downloadFileToDownloads(FILE_NAME, CONTENT_TYPE, inputStream())
            .test()
            .assertComplete()

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME)
        Assert.assertArrayEquals(FILE_CONTENT, file.readBytes())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getImageFromDownloadsFolder_emitsFileNotFoundException_whenImageNotInDownloadsFolder_onLegacyStorage() {
        // Robolectric's default public-storage directory is outside every root the FileProvider
        // config maps; relocate it under filesDir, which the config's `files-path` root covers.
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        ShadowEnvironment.setExternalStoragePublicDirectory(File(context.filesDir, "public-storage").toPath())

        dataSource.getImageFromDownloadsFolder(IMAGE_NAME)
            .test()
            .assertError(FileNotFoundException::class.java)
    }

    // MediaStore path (API 29+)

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q, Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun putImageToDownloads_completesSuccessfully_onMediaStore() {
        dataSource.putImageToDownloads(IMAGE_NAME, BITMAP)
            .test()
            .assertComplete()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q, Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun downloadFileToDownloads_completesSuccessfully_onMediaStore() {
        dataSource.downloadFileToDownloads(FILE_NAME, CONTENT_TYPE, inputStream())
            .test()
            .assertComplete()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q, Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun downloadFileToDownloads_completesSuccessfully_whenNullContentType_onMediaStore() {
        dataSource.downloadFileToDownloads(FILE_NAME, null, inputStream())
            .test()
            .assertComplete()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q, Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun downloadFileToDownloads_writesFileContent_onMediaStore() {
        dataSource.downloadFileToDownloads(FILE_NAME, CONTENT_TYPE, inputStream())
            .test()
            .assertComplete()

        Assert.assertArrayEquals(FILE_CONTENT, FakeMediaDownloadsProvider.contentOf(FILE_NAME))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q, Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun getImageFromDownloadsFolder_returnsImage_afterPutImageToDownloads_onMediaStore() {
        dataSource.putImageToDownloads(IMAGE_NAME, BITMAP)
            .test()
            .assertComplete()

        dataSource.getImageFromDownloadsFolder(IMAGE_NAME)
            .test()
            .assertValue { it.width == BITMAP.width && it.height == BITMAP.height }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q, Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun getImageFromDownloadsFolder_emitsFileNotFoundException_whenImageNotInDownloadsFolder_onMediaStore() {
        // A missing image resolves to Uri.EMPTY and an undecodable stream. By default Robolectric
        // fabricates a Bitmap for any stream; disable that so decoding fails like on a real device.
        ShadowBitmapFactory.setAllowInvalidImageData(false)
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        Shadows.shadowOf(context.contentResolver).registerInputStream(Uri.EMPTY, ByteArrayInputStream(ByteArray(0)))

        dataSource.getImageFromDownloadsFolder(IMAGE_NAME)
            .test()
            .assertError(FileNotFoundException::class.java)
    }

    /**
     * Minimal in-memory stand-in for the system MediaStore downloads provider, backing
     * `insert`/`openFile`/`query` with temp files so the API 29+ code path can be exercised
     * hermetically on the JVM.
     */
    internal class FakeMediaDownloadsProvider : android.content.ContentProvider() {

        override fun onCreate(): Boolean {
            entries.clear()
            return true
        }

        override fun insert(uri: Uri, values: ContentValues?): Uri {
            val displayName = requireNotNull(values?.getAsString(MediaStore.MediaColumns.DISPLAY_NAME))
            val id = nextId++
            entries[id] = Entry(displayName, File.createTempFile("fake_download_$id", null))
            return ContentUris.withAppendedId(uri, id)
        }

        override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
            val entry = entries[ContentUris.parseId(uri)] ?: throw FileNotFoundException(uri.toString())
            val pfdMode = if (mode.contains("w")) {
                ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE
            } else {
                ParcelFileDescriptor.MODE_READ_ONLY
            }
            return ParcelFileDescriptor.open(entry.file, pfdMode)
        }

        override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor {
            val cursor = MatrixCursor(
                arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME, MediaStore.Downloads.SIZE)
            )
            entries
                .filterValues { selectionArgs == null || it.displayName == selectionArgs.first() }
                .forEach { (id, entry) -> cursor.addRow(arrayOf(id, entry.displayName, entry.file.length())) }
            return cursor
        }

        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0

        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

        override fun getType(uri: Uri): String? = null

        private data class Entry(val displayName: String, val file: File)

        companion object {
            private val entries: MutableMap<Long, Entry> = mutableMapOf()
            private var nextId: Long = 1L

            fun contentOf(displayName: String): ByteArray =
                requireNotNull(entries.values.find { it.displayName == displayName }) { "No entry named $displayName" }
                    .file
                    .readBytes()
        }
    }

    private companion object {
        private const val IMAGE_NAME: String = "IMAGE_NAME"
        private const val FILE_NAME: String = "FILE_NAME"
        private const val CONTENT_TYPE: String = "content_type"
        private val BITMAP: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
        private val FILE_CONTENT: ByteArray = "test data".toByteArray()

        private fun inputStream(): InputStream = ByteArrayInputStream(FILE_CONTENT)
    }
}
