package com.glia.widgets.callvisualizer.controller

import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.callvisualizer.VisitorCodeContract
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeRepository
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.engagement.GliaEngagementRepository
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import java.util.function.Consumer

internal class VisitorCodeControllerTest {

    private val dialogController = mock(DialogController::class.java)
    private val visitorCodeRepository = mock(VisitorCodeRepository::class.java)
    private val engagementRepository = mock(GliaEngagementRepository::class.java)
    private val controller =
        VisitorCodeController(dialogController, visitorCodeRepository, engagementRepository)
    private val view = mock(VisitorCodeContract.View::class.java)
    private val refreshTime = 1000L

    @Before
    fun setView() {
        controller.setView(view)
        verify(view).notifySetupComplete()
        verifyNoMoreInteractions(view)
        verifyNoInteractions(dialogController, visitorCodeRepository)
        reset(view, dialogController, visitorCodeRepository)
    }

    @Test
    fun onCloseButtonClicked() {
        controller.onCloseButtonClicked()
        verify(dialogController).dismissCurrentDialog()
        verify(view).destroyTimer()
        verifyNoMoreInteractions(dialogController, view)
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
        verifyNoInteractions(dialogController)

        RxJavaPlugins.setComputationSchedulerHandler { null }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { null }
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
        verifyNoInteractions(dialogController)

        RxJavaPlugins.setComputationSchedulerHandler { null }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { null }
    }

    @Test
    fun `auto close is triggered when engagement starts`() {
        val mockEngagement = mock(OmnibrowseEngagement::class.java)
        val callbackCaptor = argumentCaptor<Consumer<OmnibrowseEngagement>>()
        verify(engagementRepository).listenForCallVisualizerEngagement(callbackCaptor.capture())

        callbackCaptor.firstValue.accept(mockEngagement)

        verify(dialogController).dismissVisitorCodeDialog()
    }

    @Test
    fun onDestroy() {
        controller.onDestroy()
        verify(view).destroyTimer()
        verifyNoMoreInteractions(view)
        verifyNoInteractions(dialogController, visitorCodeRepository)
    }
}
