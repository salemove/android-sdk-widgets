package com.glia.widgets.callvisualizer.controller

import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.callvisualizer.VisitorCodeContract
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeRepository
import com.glia.widgets.core.dialog.DialogController
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class VisitorCodeControllerTest {

    private val dialogController = mock(DialogController::class.java)
    private val repository = mock(VisitorCodeRepository::class.java)
    private val controller = VisitorCodeController(dialogController, repository)
    private val view = mock(VisitorCodeContract.View::class.java)
    private val refreshTime = 1000L

    @Before
    fun setView() {
        controller.setView(view)
        verify(view).notifySetupComplete()
        verifyNoMoreInteractions(view)
        verifyNoInteractions(dialogController, repository)
        reset(view, dialogController, repository)
    }

    @Test
    fun onCloseButtonClicked() {
        controller.onCloseButtonClicked()
        verify(dialogController).dismissCurrentDialog()
        verifyNoMoreInteractions(dialogController)
        verifyNoInteractions(view, repository)
    }

    @Test
    fun `onLoadVisitorCode success`() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        val vc = mock(VisitorCode::class.java)
        whenever(repository.getVisitorCode()).thenReturn(Observable.just(vc))
        whenever(vc.duration).thenReturn(refreshTime)
        controller.onLoadVisitorCode()
        verify(view).startLoading()
        verify(repository).getVisitorCode()
        verify(view).showVisitorCode(vc)
        verify(view).setTimer(vc.duration)
        verifyNoMoreInteractions(view, repository)
        verifyNoInteractions(dialogController)

        RxJavaPlugins.setComputationSchedulerHandler { null }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { null }
    }

    @Test
    fun `onLoadVisitorCode failure`() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        val cause = Throwable("error")
        whenever(repository.getVisitorCode()).thenReturn(Observable.error(cause))
        controller.onLoadVisitorCode()
        verify(view).startLoading()
        verify(repository).getVisitorCode()
        verify(view).showError(cause)
        verifyNoMoreInteractions(view, repository)
        verifyNoInteractions(dialogController)

        RxJavaPlugins.setComputationSchedulerHandler { null }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { null }
    }

    @Test
    fun onDestroy() {
        controller.onDestroy()
        verify(view).cleanUpOnDestroy()
        verifyNoMoreInteractions(view)
        verifyNoInteractions(dialogController, repository)
    }
}
