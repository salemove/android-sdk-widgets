package com.glia.widgets.view.head

import android.app.Activity
import android.view.View
import android.view.Window
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.engagement.EngagementStateUseCase
import com.glia.widgets.engagement.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadContract
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadController
import com.glia.widgets.view.head.controller.ApplicationChatHeadLayoutController
import com.glia.widgets.view.head.controller.ServiceChatHeadController
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ActivityWatcherForChatHeadTest {

    private val watcher = Mockito.mock(ActivityWatcherForChatHeadContract.Watcher::class.java)
    private val serviceChatHeadController = Mockito.mock(ServiceChatHeadController::class.java)
    private val applicationChatHeadController =
        Mockito.mock(ApplicationChatHeadLayoutController::class.java)
    private val screenSharingController = Mockito.mock(ScreenSharingController::class.java)
    private val isFromCallScreenUseCase = Mockito.mock(IsFromCallScreenUseCase::class.java)
    private val updateFromCallScreenUseCase = Mockito.mock(UpdateFromCallScreenUseCase::class.java)
    private val isCurrentEngagementCallVisualizerUseCase = Mockito.mock(IsCurrentEngagementCallVisualizerUseCase::class.java)
    private val engagementStateUseCase = Mockito.mock(EngagementStateUseCase::class.java)
    private val controller = ActivityWatcherForChatHeadController(
        serviceChatHeadController,
        applicationChatHeadController,
        screenSharingController,
        engagementStateUseCase,
        isFromCallScreenUseCase,
        updateFromCallScreenUseCase,
        isCurrentEngagementCallVisualizerUseCase
    )
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
        whenever(isCurrentEngagementCallVisualizerUseCase()).thenReturn(true)
        `onActivityResumed callbacks are set when call or chat are not active`()
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.screenSharingViewCallback?.onScreenSharingRequestSuccess()
        verify(watcher).fetchGliaOrRootView()
        verify(serviceChatHeadController).onResume(view)
        verify(applicationChatHeadController, times(2)).onResume(any())
    }

    @Test
    fun `onActivityPaused cleanup of callbacks`() {
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.onActivityPaused()
        verify(watcher).fetchGliaOrRootView()
        verify(screenSharingController).removeViewCallback(anyOrNull())
        verify(serviceChatHeadController).onPause(view)
        verify(applicationChatHeadController).onPause(view.javaClass.simpleName)
    }

    private fun `onActivityResumed callbacks are set when call or chat are not active`() {
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.onActivityResumed()
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
