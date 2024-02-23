package com.glia.widgets.callvisualizer

import com.glia.widgets.engagement.domain.EndScreenSharingUseCase
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions

internal class EndScreenSharingControllerTest {

    private val endScreenSharingUseCase: EndScreenSharingUseCase = mock()
    private val controller = EndScreenSharingController(endScreenSharingUseCase)
    private val view = mock(EndScreenSharingContract.View::class.java)

    @Before
    fun setup() {
        controller.setView(view)
    }

    @Test
    fun setView() {
        verifyNoInteractions(view)
    }

    @Test
    fun onBackArrowClicked() {
        controller.onBackArrowClicked()
        verify(view).finish()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onEndScreenSharingButtonClicked() {
        controller.onEndScreenSharingButtonClicked()
        verify(view).stopScreenSharing()
        verify(view).finish()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onDestroy() {
        controller.onDestroy()
        verifyNoInteractions(view)
    }

    @Test
    fun onForceStopScreenSharing() {
        controller.onForceStopScreenSharing()
        verify(endScreenSharingUseCase).invoke()
        verifyNoMoreInteractions(endScreenSharingUseCase)
    }
}
