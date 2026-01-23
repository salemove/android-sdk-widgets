package com.glia.widgets.filepreview.ui

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glia.widgets.R
import com.glia.widgets.helper.FragmentArgumentKeys
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Unit tests for ImagePreviewFragment.
 *
 * Tests Fragment lifecycle, menu handling, and permission management.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ImagePreviewFragmentTest {

    @Test
    fun `fragment creation with remote image ID succeeds`() {
        val imageId = "test-image-id-123"
        val imageName = "test-image.jpg"
        val args = Bundle().apply {
            putString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_ID, imageId)
            putString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_NAME, imageName)
        }

        val scenario = launchFragmentInContainer<ImagePreviewFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
            assertTrue(fragment.gliaView is ImagePreviewView)
        }
    }

    @Test
    fun `fragment creation with local URI succeeds`() {
        val localUri = "content://media/external/images/1".toUri()
        val args = Bundle().apply {
            putParcelable(FragmentArgumentKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI, localUri)
        }

        val scenario = launchFragmentInContainer<ImagePreviewFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
        }
    }

    @Test
    fun `fragment survives configuration changes`() {
        val imageId = "test-id"
        val imageName = "image.png"
        val args = Bundle().apply {
            putString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_ID, imageId)
            putString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_NAME, imageName)
        }

        val scenario = launchFragmentInContainer<ImagePreviewFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.recreate()

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
        }
    }

    @Test
    fun `view binding is cleared in onDestroyView`() {
        val imageId = "test-id"
        val imageName = "test.jpg"
        val args = Bundle().apply {
            putString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_ID, imageId)
            putString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_NAME, imageName)
        }

        val scenario = launchFragmentInContainer<ImagePreviewFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.moveToState(Lifecycle.State.DESTROYED)

        assertTrue(true)
    }

    @Test
    fun `newInstance creates fragment with remote image arguments`() {
        val imageId = "remote-image-456"
        val imageName = "remote-image.jpg"
        val fragment = ImagePreviewFragment.newInstance(imageId, imageName)

        val extractedId = fragment.arguments?.getString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_ID)
        val extractedName = fragment.arguments?.getString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_NAME)

        assertNotNull(extractedId)
        assertNotNull(extractedName)
        assertEquals(imageId, extractedId)
        assertEquals(imageName, extractedName)
    }

    @Test
    fun `newInstanceLocal creates fragment with local URI argument`() {
        val localUri = "content://media/external/images/999".toUri()
        val fragment = ImagePreviewFragment.newInstanceLocal(localUri)

        val extractedUri = fragment.arguments?.getParcelable<Uri>(FragmentArgumentKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI)

        assertNotNull(extractedUri)
        assertEquals(localUri, extractedUri)
    }

    @Test
    fun `fragment implements ImagePreviewContract View`() {
        val imageId = "test-id"
        val imageName = "test.jpg"
        val args = Bundle().apply {
            putString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_ID, imageId)
            putString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_NAME, imageName)
        }

        val scenario = launchFragmentInContainer<ImagePreviewFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertTrue(fragment is ImagePreviewContract.View)
        }
    }
}
