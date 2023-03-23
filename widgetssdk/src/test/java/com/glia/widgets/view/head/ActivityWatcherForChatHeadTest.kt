package com.glia.widgets.view.head

import android.app.Activity
import android.view.View
import android.view.Window
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadContract
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadController
import com.glia.widgets.view.head.controller.ServiceChatHeadController
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.*

internal class ActivityWatcherForChatHeadTest {

    private val watcher = Mockito.mock(ActivityWatcherForChatHeadContract.Watcher::class.java)
    private val serviceChatHeadController = Mockito.mock(ServiceChatHeadController::class.java)
    private val screenSharingController = Mockito.mock(ScreenSharingController::class.java)
    private val controller = ActivityWatcherForChatHeadController(serviceChatHeadController, screenSharingController)
    private val activity = Mockito.mock(Activity::class.java)
    private val view = Mockito.mock(View::class.java)

    @Before
    fun setup() {
        val window = Mockito.mock(Window::class.java)
        val view = Mockito.mock(View::class.java)
        whenever(activity.window).thenReturn(window)
        whenever(window.decorView).thenReturn(view)
        controller.setWatcher(watcher)
        cleanup()
        resetMocks()
    }

    @Test
    fun `onActivityResumed bubble is resumed when onScreenSharingStarted`() {
        `onActivityResumed callbacks are set when call or chat are not active`()
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.screenSharingViewCallback?.onScreenSharingStarted()
        verify(watcher).fetchGliaOrRootView()
        verify(serviceChatHeadController).onResume(view)
    }

    @Test
    fun `onActivityPaused cleanup of callbacks`() {
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.onActivityPaused()
        verify(watcher).fetchGliaOrRootView()
        verify(screenSharingController).removeViewCallback(anyOrNull())
        verify(serviceChatHeadController).onPause(view)
    }

    private fun `onActivityResumed callbacks are set when call or chat are not active`() {
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.onActivityResumed(activity)
        verify(serviceChatHeadController).onResume(view)
        verify(screenSharingController).setViewCallback(anyOrNull())
        verify(watcher).fetchGliaOrRootView()
        cleanup()
        resetMocks()
    }

    private fun resetMocks() {
        reset(watcher, serviceChatHeadController, screenSharingController)
    }

    @After
    fun cleanup() {
        verifyNoMoreInteractions(watcher, serviceChatHeadController, screenSharingController)
    }
}
