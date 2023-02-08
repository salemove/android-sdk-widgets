package com.glia.widgets.callvisualizer.controller

import android.app.Activity
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.core.dialog.DialogController
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

internal class CallVisualizerMediaUpgradeControllerTest {
    private lateinit var callVisualizerMediaUpgradeController: CallVisualizerMediaUpgradeController
    private lateinit var isCallOrChatScreenActiveUseCase: IsCallOrChatScreenActiveUseCase
    private lateinit var dialogController: DialogController

    @Before
    fun setUp() {
        isCallOrChatScreenActiveUseCase = mock(IsCallOrChatScreenActiveUseCase::class.java)
        dialogController = mock(DialogController::class.java)
        callVisualizerMediaUpgradeController = CallVisualizerMediaUpgradeController(
            dialogController,
            isCallOrChatScreenActiveUseCase
        )
    }

    @Test
    fun addDialogCallback_callsSetupDialogCallback_whenIsCallOrChatScreenActiveUseCaseFalse() {
        val resumedActivity = Activity()
        `when`(isCallOrChatScreenActiveUseCase(resumedActivity)).thenReturn(false)

        callVisualizerMediaUpgradeController.addDialogCallback(resumedActivity)

        verify(dialogController, times(1)).addCallback(any(DialogController.Callback::class.java))
    }

    @Test
    fun addDialogCallback_doesNotCallSetupDialogCallback_whenIsCallOrChatScreenActiveUseCaseTrue() {
        val resumedActivity = Activity()
        `when`(isCallOrChatScreenActiveUseCase(resumedActivity)).thenReturn(true)

        callVisualizerMediaUpgradeController.addDialogCallback(resumedActivity)

        verify(dialogController, never()).addCallback(any(DialogController.Callback::class.java))
    }
}
