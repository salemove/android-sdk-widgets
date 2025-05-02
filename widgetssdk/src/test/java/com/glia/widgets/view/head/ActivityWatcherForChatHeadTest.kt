package com.glia.widgets.view.head

import android.app.Activity
import android.view.View
import android.view.Window
import com.glia.androidsdk.Engagement
import com.glia.widgets.chat.ChatView
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.EndedBy
import com.glia.widgets.engagement.ScreenSharingState
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadContract
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadController
import com.glia.widgets.view.head.controller.ApplicationChatHeadLayoutController
import com.glia.widgets.view.head.controller.ServiceChatHeadController
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ActivityWatcherForChatHeadTest {
    private val screenSharingStateFlowable: PublishProcessor<ScreenSharingState> = PublishProcessor.create()
    private val engagementStateFlowable: PublishProcessor<State> = PublishProcessor.create()
    private val watcher = Mockito.mock(ActivityWatcherForChatHeadContract.Watcher::class.java)
    private val serviceChatHeadController = Mockito.mock(ServiceChatHeadController::class.java)
    private val applicationChatHeadController =
        Mockito.mock(ApplicationChatHeadLayoutController::class.java)
    private val isFromCallScreenUseCase = Mockito.mock(IsFromCallScreenUseCase::class.java)
    private val updateFromCallScreenUseCase = Mockito.mock(UpdateFromCallScreenUseCase::class.java)
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase = mock()
    private val engagementStateUseCase: EngagementStateUseCase = mock {
        on { invoke() } doReturn engagementStateFlowable
    }
    private val screenSharingUseCase: ScreenSharingUseCase = mock {
        on { invoke() } doReturn screenSharingStateFlowable
    }
    private val controller = ActivityWatcherForChatHeadController(
        serviceChatHeadController,
        applicationChatHeadController,
        screenSharingUseCase,
        engagementStateUseCase,
        isFromCallScreenUseCase,
        updateFromCallScreenUseCase,
        isCurrentEngagementCallVisualizerUseCase
    )
    private val activity = Mockito.mock(Activity::class.java)
    private val view = Mockito.mock(View::class.java)

    @Before
    fun setup() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        val window = Mockito.mock(Window::class.java)
        val view = Mockito.mock(View::class.java)
        whenever(activity.window).thenReturn(window)
        whenever(window.decorView).thenReturn(view)
        controller.setWatcher(watcher)
        controller.init()

        verify(engagementStateUseCase).invoke()
        verify(screenSharingUseCase).invoke()

        cleanup()
        resetMocks()
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `onActivityResumed bubble is resumed`() {
        `onActivityResumed callbacks are set when call or chat are not active`()
        verify(applicationChatHeadController).onResume(any())
    }

    @Test
    fun `bubble is resumed when screenSharing is started and current engagement is CV`() {
        mockShouldShowChatHead()

        screenSharingStateFlowable.onNext(ScreenSharingState.RequestAccepted)

        verifyBubbleIsShowed()
    }

    @Test
    fun `onActivityPaused cleanup of callbacks`() {
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.onActivityPaused()
        verify(watcher).fetchGliaOrRootView()
        verify(serviceChatHeadController).onPause(view)
        verify(applicationChatHeadController).onPause(view.javaClass.simpleName)
    }

    @Test
    fun `bubble will be removed when engagement or queueing ended`() {
        engagementStateFlowable.onNext(State.EngagementEnded(true, EndedBy.OPERATOR, Engagement.ActionOnEnd.UNKNOWN, mock()))
        engagementStateFlowable.onNext(State.EngagementEnded(false, EndedBy.CLEAR_STATE, Engagement.ActionOnEnd.UNKNOWN, mock()))
        engagementStateFlowable.onNext(State.QueueUnstaffed)
        engagementStateFlowable.onNext(State.UnexpectedErrorHappened)
        engagementStateFlowable.onNext(State.QueueingCanceled)

        verify(watcher, times(5)).removeChatHeadLayoutIfPresent()
        verifyNoMoreInteractions(watcher)
    }

    @Test
    fun `bubble will be updated when OmniCoreEngagement is started`() {
        mockShouldShowChatHead()

        engagementStateFlowable.onNext(State.EngagementStarted(false))
        verifyBubbleIsShowed(times = 2)
    }

    @Test
    fun `bubble will be updated when OmniBrowseEngagement is started`() {
        mockShouldShowChatHead()

        engagementStateFlowable.onNext(State.EngagementStarted(true))
        verifyBubbleIsShowed(times = 2)
    }

    @Test
    fun `bubble will be updated when PreQueuing`() {
        mockShouldShowChatHead()

        engagementStateFlowable.onNext(State.PreQueuing(MediaType.TEXT))
        verifyBubbleIsShowed(times = 2)
    }

    @Test
    fun `bubble will be updated when Queueing is started`() {
        mockShouldShowChatHead()

        engagementStateFlowable.onNext(State.Queuing("", MediaType.TEXT))
        verifyBubbleIsShowed(times = 2)
    }

    @Test
    fun `bubble will be updated when Engagement is updated`() {
        mockShouldShowChatHead()

        engagementStateFlowable.onNext(mock<State.Update>())
        verifyBubbleIsShowed(times = 2)
    }

    private fun mockShouldShowChatHead() {
        val mockView: ChatView = mock()
        whenever(watcher.fetchGliaOrRootView()) doReturn mockView
        whenever(isCurrentEngagementCallVisualizerUseCase()) doReturn true
        whenever(applicationChatHeadController.shouldShow(any())) doReturn true
    }


    private fun verifyBubbleIsShowed(times: Int = 1) {
        verify(watcher, times(times)).fetchGliaOrRootView()
        verify(serviceChatHeadController).onResume(any())
        verify(applicationChatHeadController).onResume(any())
        verify(applicationChatHeadController, times(times)).shouldShow(any())
        verify(watcher).addChatHeadLayoutIfAbsent()
        verify(applicationChatHeadController).updateChatHeadView()
        verifyNoMoreInteractions(watcher, serviceChatHeadController, applicationChatHeadController)
    }

    private fun `onActivityResumed callbacks are set when call or chat are not active`() {
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.onActivityResumed()
        verify(serviceChatHeadController).onResume(view)
        verify(watcher).fetchGliaOrRootView()
        cleanup()
        resetMocks()
    }

    private fun resetMocks() {
        reset(watcher, serviceChatHeadController, screenSharingUseCase)
    }

    @After
    fun cleanup() {
        verifyNoMoreInteractions(watcher, serviceChatHeadController, screenSharingUseCase)
    }
}
