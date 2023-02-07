package com.glia.widgets.callvisualizer.controller

import android.app.Activity
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.core.dialog.DialogController
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

internal class CallVisualizerControllerTest {
    private lateinit var callVisualizerController: CallVisualizerController
    private lateinit var isCallOrChatScreenActiveUseCase: IsCallOrChatScreenActiveUseCase
    private lateinit var dialogController: DialogController

    @Before
    fun setUp() {
        isCallOrChatScreenActiveUseCase = mock(IsCallOrChatScreenActiveUseCase::class.java)
        dialogController = mock(DialogController::class.java)
        callVisualizerController = CallVisualizerController(
            dialogController,
            isCallOrChatScreenActiveUseCase
        )
    }

    @Test
    fun addDialogCallback_callsSetupDialogCallback_whenIsCallOrChatScreenActiveUseCaseFalse() {
        val resumedActivity = Activity()
        `when`(isCallOrChatScreenActiveUseCase(resumedActivity)).thenReturn(false)

        callVisualizerController.addDialogCallback(resumedActivity)

        verify(dialogController, times(1)).addCallback(any(DialogController.Callback::class.java))
    }

    @Test
    fun addDialogCallback_doesNotCallSetupDialogCallback_whenIsCallOrChatScreenActiveUseCaseTrue() {
        val resumedActivity = Activity()
        `when`(isCallOrChatScreenActiveUseCase(resumedActivity)).thenReturn(true)

        callVisualizerController.addDialogCallback(resumedActivity)

        verify(dialogController, never()).addCallback(any(DialogController.Callback::class.java))
    }
}
