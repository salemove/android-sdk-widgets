package com.glia.widgets.helper

import android.content.Intent
import android.net.Uri
import com.glia.widgets.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IntentHelperImplTest {

    private val intentHelper: IntentHelperImpl = IntentHelperImpl()
    private val uri: Uri = Uri.parse("content://com.glia.test.fileprovider/cache/file.mp4")

    @Test
    fun `openFileIntent grants read permission only`() {
        val intent = intentHelper.openFileIntent(uri, "video/mp4")

        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertEquals(0, intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    @Test
    fun `openFileIntent sets view action, data, type and clip data`() {
        val intent = intentHelper.openFileIntent(uri, "video/mp4")

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(uri, intent.data)
        assertEquals("video/mp4", intent.type)
        assertNotNull(intent.clipData)
        assertEquals(uri, intent.clipData?.getItemAt(0)?.uri)
    }

    @Test
    fun `shareLocalImageIntent sets stream, mime type and read permission`() {
        val intent = intentHelper.shareLocalImageIntent(uri, "image/png")

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals(uri, intent.getParcelableExtra(Intent.EXTRA_STREAM))
        assertEquals("image/png", intent.type)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertEquals(0, intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    @Test
    fun `shareLocalImageIntent falls back to generic image mime type when mime type is null`() {
        val intent = intentHelper.shareLocalImageIntent(uri, null)

        assertEquals(Constants.MIME_TYPE_IMAGES, intent.type)
    }
}
