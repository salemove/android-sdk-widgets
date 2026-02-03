package com.glia.widgets.webbrowser

import android.net.Uri
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class WebBrowserViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: WebBrowserViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WebBrowserViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initialize tests

    @Test
    fun `initial state has null values and isLoaded false`() {
        val state = viewModel.state.value

        assertNull(state.title)
        assertNull(state.url)
        assertFalse(state.isLoaded)
    }

    @Test
    fun `initialize sets title and url in state`() = runTest {
        viewModel.processIntent(WebBrowserIntent.Initialize(TEST_TITLE, TEST_URL))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(TEST_TITLE, state.title)
        assertEquals(TEST_URL, state.url)
        assertTrue(state.isLoaded)
    }

    @Test
    fun `initialize with null title sets only url`() = runTest {
        viewModel.processIntent(WebBrowserIntent.Initialize(null, TEST_URL))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.title)
        assertEquals(TEST_URL, state.url)
        assertTrue(state.isLoaded)
    }

    @Test
    fun `initialize called twice does not update state again`() = runTest {
        // First initialization
        viewModel.processIntent(WebBrowserIntent.Initialize(TEST_TITLE, TEST_URL))
        testDispatcher.scheduler.advanceUntilIdle()

        val firstState = viewModel.state.value
        assertTrue(firstState.isLoaded)

        // Second initialization with different values should be ignored
        viewModel.processIntent(WebBrowserIntent.Initialize("Different Title", "https://different.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        // State should remain the same
        val secondState = viewModel.state.value
        assertEquals(TEST_TITLE, secondState.title)
        assertEquals(TEST_URL, secondState.url)
    }

    // endregion

    // region OnLinkClicked tests

    @Test
    fun `onLinkClicked emits OpenExternalLink effect`() = runTest {
        val uri: Uri = mock {
            on { toString() } doReturn "https://example.com/page"
        }

        viewModel.effect.test {
            viewModel.processIntent(WebBrowserIntent.OnLinkClicked(uri))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is WebBrowserEffect.OpenExternalLink)
            assertEquals(uri, (effect as WebBrowserEffect.OpenExternalLink).uri)
        }
    }

    // endregion

    // region Close tests

    @Test
    fun `close emits Finish effect`() = runTest {
        viewModel.effect.test {
            viewModel.processIntent(WebBrowserIntent.Close)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(WebBrowserEffect.Finish, awaitItem())
        }
    }

    // endregion

    companion object {
        private const val TEST_TITLE = "Privacy Policy"
        private const val TEST_URL = "https://example.com/privacy"
    }
}