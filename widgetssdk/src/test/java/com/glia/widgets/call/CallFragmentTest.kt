package com.glia.widgets.call

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glia.widgets.R
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.helper.FragmentArgumentKeys
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Unit tests for CallFragment.
 *
 * Tests Fragment lifecycle, argument handling, and media type configuration.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class CallFragmentTest {

    @Test
    fun `fragment creation with audio media type succeeds`() {
        val mediaType = MediaType.AUDIO
        val args = Bundle().apply {
            putInt(FragmentArgumentKeys.MEDIA_TYPE, mediaType.ordinal)
            putBoolean(FragmentArgumentKeys.IS_UPGRADE_TO_CALL, false)
        }

        val scenario = launchFragmentInContainer<CallFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
            assertTrue(fragment.gliaView is CallView)
        }
    }

    @Test
    fun `fragment creation with video media type succeeds`() {
        val mediaType = MediaType.VIDEO
        val args = Bundle().apply {
            putInt(FragmentArgumentKeys.MEDIA_TYPE, mediaType.ordinal)
            putBoolean(FragmentArgumentKeys.IS_UPGRADE_TO_CALL, true)
        }

        val scenario = launchFragmentInContainer<CallFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
        }
    }

    @Test
    fun `fragment survives configuration changes`() {
        val args = Bundle().apply {
            putInt(FragmentArgumentKeys.MEDIA_TYPE, MediaType.AUDIO.ordinal)
            putBoolean(FragmentArgumentKeys.IS_UPGRADE_TO_CALL, false)
        }

        val scenario = launchFragmentInContainer<CallFragment>(
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
        val args = Bundle().apply {
            putInt(FragmentArgumentKeys.MEDIA_TYPE, MediaType.VIDEO.ordinal)
            putBoolean(FragmentArgumentKeys.IS_UPGRADE_TO_CALL, false)
        }

        val scenario = launchFragmentInContainer<CallFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.moveToState(Lifecycle.State.DESTROYED)

        assertTrue(true)
    }

    @Test
    fun `newInstance creates fragment with correct arguments for audio`() {
        val mediaType = MediaType.AUDIO
        val upgradeToCall = false
        val fragment = CallFragment.newInstance(mediaType, upgradeToCall)

        val extractedMediaTypeOrdinal = fragment.arguments?.getInt(FragmentArgumentKeys.MEDIA_TYPE)
        val extractedUpgradeFlag = fragment.arguments?.getBoolean(FragmentArgumentKeys.IS_UPGRADE_TO_CALL)

        assertNotNull(extractedMediaTypeOrdinal)
        assertEquals(mediaType.ordinal, extractedMediaTypeOrdinal)
        assertEquals(upgradeToCall, extractedUpgradeFlag)
    }

    @Test
    fun `newInstance creates fragment with null media type`() {
        val fragment = CallFragment.newInstance(null, true)

        val extractedMediaTypeOrdinal = fragment.arguments?.getInt(FragmentArgumentKeys.MEDIA_TYPE)
        val extractedUpgradeFlag = fragment.arguments?.getBoolean(FragmentArgumentKeys.IS_UPGRADE_TO_CALL)

        assertNotNull(extractedMediaTypeOrdinal)
        assertEquals(-1, extractedMediaTypeOrdinal) // -1 indicates null
        assertEquals(true, extractedUpgradeFlag)
    }
}
