package com.glia.widgets.callvisualizer.controller

import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.callvisualizer.VisitorCodeContract
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeRepository
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

internal class VisitorCodeControllerTest {
    private val engagementState = PublishProcessor.create<State>()

    private val engagementStateUseCase: EngagementStateUseCase = org.mockito.kotlin.mock {
        on { invoke() } doReturn engagementState
    }
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase = mock()
    private val callVisualizerController: CallVisualizerContract.Controller = mock()
    private val visitorCodeRepository: VisitorCodeRepository = mock()
    private val controller =
        VisitorCodeController(callVisualizerController, visitorCodeRepository, engagementStateUseCase, isQueueingOrLiveEngagementUseCase)
    private val view: VisitorCodeContract.View = mock()
    private val refreshTime = 1000L

    @Before
    fun setView() {
        controller.setView(view)
        verify(view).notifySetupComplete()
        verifyNoMoreInteractions(view)
        verifyNoInteractions(callVisualizerController, visitorCodeRepository)
        reset(view, callVisualizerController, visitorCodeRepository)
    }

    @Test
    fun onCloseButtonClicked() {
        controller.onCloseButtonClicked()
        verify(callVisualizerController).dismissVisitorCodeDialog()
        verify(view).destroyTimer()
        verifyNoMoreInteractions(callVisualizerController, view)
        verifyNoInteractions(visitorCodeRepository)
    }

    @Test
    fun `onLoadVisitorCode success`() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        val vc = mock(VisitorCode::class.java)
        whenever(visitorCodeRepository.getVisitorCode()).thenReturn(Observable.just(vc))
        whenever(vc.duration).thenReturn(refreshTime)
        controller.onLoadVisitorCode()
        verify(view).startLoading()
        verify(visitorCodeRepository).getVisitorCode()
        verify(view).showVisitorCode(vc)
        verify(view).setTimer(vc.duration)
        verifyNoMoreInteractions(view, visitorCodeRepository)
        verifyNoInteractions(callVisualizerController)
    }

    @Test
    fun `onLoadVisitorCode failure`() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        val cause = Throwable("error")
        whenever(visitorCodeRepository.getVisitorCode()).thenReturn(Observable.error(cause))
        controller.onLoadVisitorCode()
        verify(view).startLoading()
        verify(visitorCodeRepository).getVisitorCode()
        verify(view).showError(cause)
        verifyNoMoreInteractions(view, visitorCodeRepository)
        verifyNoInteractions(callVisualizerController)
    }

    @Test
    fun `auto close is triggered when engagement starts`() {
        engagementState.onNext(State.StartedCallVisualizer)

        verify(view).destroyTimer()
        verifyNoInteractions(callVisualizerController)
    }

    @Test
    fun onDestroy() {
        controller.onDestroy()
        verify(view).destroyTimer()
        verifyNoMoreInteractions(view)
        verifyNoInteractions(callVisualizerController, visitorCodeRepository)
    }
}
