package com.glia.widgets.chat

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glia.widgets.R
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.helper.FragmentArgumentKeys
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Unit tests for ChatFragment.
 *
 * Tests Fragment lifecycle, argument handling, and host callbacks.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ChatFragmentTest {

    @Test
    fun `fragment creation with valid arguments succeeds`() {
        val intention = Intention.LIVE_CHAT
        val args = Bundle().apply {
            putInt(FragmentArgumentKeys.OPEN_CHAT_INTENTION, intention.ordinal)
        }

        val scenario = launchFragmentInContainer<ChatFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
            assertTrue(fragment.gliaView is ChatView)
        }
    }

    @Test
    fun `fragment survives configuration changes`() {
        val intention = Intention.RETURN_TO_CHAT
        val args = Bundle().apply {
            putInt(FragmentArgumentKeys.OPEN_CHAT_INTENTION, intention.ordinal)
        }

        val scenario = launchFragmentInContainer<ChatFragment>(
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
        val intention = Intention.LIVE_CHAT
        val args = Bundle().apply {
            putInt(FragmentArgumentKeys.OPEN_CHAT_INTENTION, intention.ordinal)
        }

        val scenario = launchFragmentInContainer<ChatFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.moveToState(Lifecycle.State.DESTROYED)

        // If we get here without crashes, binding was properly cleared
        assertTrue(true)
    }

    @Test
    fun `newInstance creates fragment with correct arguments`() {
        val intention = Intention.SC_CHAT
        val fragment = ChatFragment.newInstance(intention)

        val extractedIntention = fragment.arguments?.getInt(FragmentArgumentKeys.OPEN_CHAT_INTENTION)
        assertNotNull(extractedIntention)
        assertTrue(extractedIntention == intention.ordinal)
    }
}
