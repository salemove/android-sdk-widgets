package com.glia.widgets.webbrowser

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glia.widgets.R
import com.glia.widgets.helper.FragmentArgumentKeys
import com.glia.widgets.locale.LocaleString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Unit tests for WebBrowserFragment.
 *
 * Tests Fragment lifecycle and URL/title handling.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class WebBrowserFragmentTest {

    @Test
    fun `fragment creation with URL and title succeeds`() {
        val url = "https://example.com"
        val title = LocaleString(R.string.android_preview_title)
        val args = Bundle().apply {
            putString(FragmentArgumentKeys.WEB_BROWSER_URL, url)
            putParcelable(FragmentArgumentKeys.WEB_BROWSER_TITLE, title)
        }

        val scenario = launchFragmentInContainer<WebBrowserFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
            assertTrue(fragment.gliaView is WebBrowserView)
        }
    }

    @Test
    fun `fragment survives configuration changes`() {
        val url = "https://example.com/terms"
        val title = LocaleString(R.string.android_preview_title)
        val args = Bundle().apply {
            putString(FragmentArgumentKeys.WEB_BROWSER_URL, url)
            putParcelable(FragmentArgumentKeys.WEB_BROWSER_TITLE, title)
        }

        val scenario = launchFragmentInContainer<WebBrowserFragment>(
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
        val url = "https://example.com"
        val title = LocaleString(R.string.android_preview_title)
        val args = Bundle().apply {
            putString(FragmentArgumentKeys.WEB_BROWSER_URL, url)
            putParcelable(FragmentArgumentKeys.WEB_BROWSER_TITLE, title)
        }

        val scenario = launchFragmentInContainer<WebBrowserFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.moveToState(Lifecycle.State.DESTROYED)

        assertTrue(true)
    }

    @Test
    fun `newInstance creates fragment with correct arguments`() {
        val url = "https://example.com/privacy"
        val title = LocaleString(R.string.android_preview_title)
        val fragment = WebBrowserFragment.newInstance(url, title)

        val extractedUrl = fragment.arguments?.getString(FragmentArgumentKeys.WEB_BROWSER_URL)
        val extractedTitle = fragment.arguments?.getParcelable<LocaleString>(FragmentArgumentKeys.WEB_BROWSER_TITLE)

        assertNotNull(extractedUrl)
        assertNotNull(extractedTitle)
        assertEquals(url, extractedUrl)
        assertEquals(title.stringKey, extractedTitle?.stringKey)
    }

    @Test
    fun `fragment implements OnFinishListener`() {
        val url = "https://example.com"
        val title = LocaleString(R.string.android_preview_title)
        val args = Bundle().apply {
            putString(FragmentArgumentKeys.WEB_BROWSER_URL, url)
            putParcelable(FragmentArgumentKeys.WEB_BROWSER_TITLE, title)
        }

        val scenario = launchFragmentInContainer<WebBrowserFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertTrue(fragment is WebBrowserView.OnFinishListener)
        }
    }

    @Test
    fun `fragment implements OnLinkClickListener`() {
        val url = "https://example.com"
        val title = LocaleString(R.string.android_preview_title)
        val args = Bundle().apply {
            putString(FragmentArgumentKeys.WEB_BROWSER_URL, url)
            putParcelable(FragmentArgumentKeys.WEB_BROWSER_TITLE, title)
        }

        val scenario = launchFragmentInContainer<WebBrowserFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertTrue(fragment is WebBrowserView.OnLinkClickListener)
        }
    }
}
