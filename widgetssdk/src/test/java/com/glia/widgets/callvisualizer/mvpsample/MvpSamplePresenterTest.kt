package com.glia.widgets.callvisualizer.mvpsample

import com.glia.widgets.callvisualizer.mvpsample.MvpSamplePresenter.Companion.EVENT_SEND_COUNTER
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.reset
import org.mockito.kotlin.verifyNoMoreInteractions
import java.lang.Exception

internal class MvpSamplePresenterTest {

    private val view = mock(MvpSampleContract.View::class.java)
    private val model = mock(MvpSampleContract.Model::class.java)
    private val presenter = MvpSamplePresenter(view, model)

    @After
    fun cleanup() {
        verifyNoMoreInteractions(view, model)
        reset(view,model)
    }

    @Test
    fun `subscribe when state is null`() {
        presenter.subscribe(null)
        verify(view).setCounterValue("0")
    }

    @Test
    fun `subscribe with state non null`() {
        presenter.subscribe(SampleState(200))
        verify(view).setCounterValue("200")
    }

    @Test
    fun getState() {
        assertEquals(presenter.getState()?.counterValue, 0)
        `subscribe with state non null`()
        assertEquals(presenter.getState()?.counterValue,  200)
    }

    @Test
    fun unsubscribe() {
        presenter.unsubscribe()
    }

    @Test
    fun onButtonClicked() {
        presenter.onButtonClicked()
        verify(model).sendCounterValue(EVENT_SEND_COUNTER, 1)
    }

    @Test
    fun `onMessageEvent success`() {
        presenter.onMessageEvent(SimpleIntEvent.Success(EVENT_SEND_COUNTER, 21))
        assertEquals(21, presenter.getState()?.counterValue)
        verify(view).setCounterValue("21")
    }

    @Test
    fun `onMessageEvent failure`() {
        presenter.onMessageEvent(SimpleIntEvent.Failure(EVENT_SEND_COUNTER, Exception()))
        verify(view).showToast()
    }
}