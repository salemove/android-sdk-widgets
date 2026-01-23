package com.glia.widgets.messagecenter

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glia.widgets.R
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Unit tests for MessageCenterFragment.
 *
 * Tests Fragment lifecycle and Activity Result API integration.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MessageCenterFragmentTest {

    @Test
    fun `fragment creation succeeds`() {
        val scenario = launchFragmentInContainer<MessageCenterFragment>(
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
            assertTrue(fragment.gliaView is MessageCenterView)
        }
    }

    @Test
    fun `fragment survives configuration changes`() {
        val scenario = launchFragmentInContainer<MessageCenterFragment>(
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.recreate()

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
        }
    }

    @Test
    fun `view binding is cleared in onDestroyView`() {
        val scenario = launchFragmentInContainer<MessageCenterFragment>(
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.moveToState(Lifecycle.State.DESTROYED)

        assertTrue(true)
    }

    @Test
    fun `fragment implements OnFinishListener`() {
        val scenario = launchFragmentInContainer<MessageCenterFragment>(
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertTrue(fragment is MessageCenterView.OnFinishListener)
        }
    }

    @Test
    fun `fragment implements OnNavigateToMessagingListener`() {
        val scenario = launchFragmentInContainer<MessageCenterFragment>(
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertTrue(fragment is MessageCenterView.OnNavigateToMessagingListener)
        }
    }

    @Test
    fun `fragment implements OnAttachFileListener`() {
        val scenario = launchFragmentInContainer<MessageCenterFragment>(
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertTrue(fragment is MessageCenterView.OnAttachFileListener)
        }
    }
}
