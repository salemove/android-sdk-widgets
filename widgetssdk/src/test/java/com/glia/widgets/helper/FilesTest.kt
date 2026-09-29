package com.glia.widgets.helper

import android.net.Uri
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FilesTest {

    @Test
    fun `native photo picker uri is detected`() {
        assertTrue(Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/1000020228").isPhotoPickerUri)
    }

    @Test
    fun `photo picker get content uri is detected`() {
        assertTrue(Uri.parse("content://media/picker_get_content/0/com.android.providers.media.photopicker/media/1").isPhotoPickerUri)
    }

    @Test
    fun `play services photo picker uri is detected`() {
        assertTrue(Uri.parse("content://com.google.android.gms.photopicker/media/1").isPhotoPickerUri)
    }

    @Test
    fun `media store uri is not a photo picker uri`() {
        assertFalse(Uri.parse("content://media/external/video/media/1").isPhotoPickerUri)
    }

    @Test
    fun `document provider uri is not a photo picker uri`() {
        assertFalse(Uri.parse("content://com.android.providers.media.documents/document/video%3A1").isPhotoPickerUri)
    }

    @Test
    fun `file provider uri is not a photo picker uri`() {
        assertFalse(Uri.parse("content://com.glia.test.com.glia.widgets.fileprovider/files/IMG_1.jpg").isPhotoPickerUri)
    }
}
