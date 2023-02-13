package com.glia.widgets.helper

import android.app.Activity
import android.app.Application
import com.glia.widgets.callvisualizer.CallVisualizerRepository
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.screensharing.ScreenSharingController
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock
import java.lang.ref.WeakReference

internal class ActivityWatcherForDialogsTest {

    @Test
    fun resumedActivity_cleared_whenActivityPaused() {
        val app = mock(Application::class.java)
        val callVisualizerRepository = mock(CallVisualizerRepository::class.java)
        val dialogController = mock(DialogController::class.java)
        val controller = CallVisualizerController(
            callVisualizerRepository,
            dialogController,
            IsCallOrChatScreenActiveUseCase())
        val activityWatcherForDialogs = ActivityWatcherForDialogs(app, controller)

        activityWatcherForDialogs.onActivityResumed(mock(Activity::class.java))
        activityWatcherForDialogs.onActivityPaused(mock(Activity::class.java))

        assertNull(activityWatcherForDialogs.resumedActivity.get())
    }

    @Test
    fun resumedActivity_saved_whenActivityResumed() {
        val app = mock(Application::class.java)
        val callVisualizerRepository = mock(CallVisualizerRepository::class.java)
        val callVisualizerController =
            CallVisualizerController(callVisualizerRepository, IsCallOrChatScreenActiveUseCase())
        val screenSharingController = mock(ScreenSharingController::class.java)
        val dialogController = mock(DialogController::class.java)
        val activityWatcherForDialogs = ActivityWatcherForDialogs(
            app,
            callVisualizerController,
        )

        activityWatcherForDialogs.onActivityResumed(mock(Activity::class.java))

        assertNotNull(activityWatcherForDialogs.resumedActivity.get())
    }

    @Test
    fun alertDialog_dismissed_whenEmitDialogStateModeNone() {
        val app = mock(Application::class.java)
        val callVisualizerRepository = mock(CallVisualizerRepository::class.java)
        val dialogController = mock(DialogController::class.java)
        val controller = CallVisualizerController(
            callVisualizerRepository,
            dialogController,
            IsCallOrChatScreenActiveUseCase())
        val activityWatcherForDialogs = ActivityWatcherForDialogs(app, controller)
        activityWatcherForDialogs.alertDialog = mock(androidx.appcompat.app.AlertDialog::class.java)
        activityWatcherForDialogs.setupDialogCallback(WeakReference(mock(Activity::class.java)))

        activityWatcherForDialogs.dialogCallback?.emitDialogState(DialogState(Dialog.MODE_NONE))

        assertNull(activityWatcherForDialogs.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeMediaUpgrade() {
        val app = mock(Application::class.java)
        val callVisualizerRepository = mock(CallVisualizerRepository::class.java)
        val dialogController = mock(DialogController::class.java)
        val controller = CallVisualizerController(
            callVisualizerRepository,
            dialogController,
            IsCallOrChatScreenActiveUseCase())
        val activityWatcherForDialogs = ActivityWatcherForDialogs(app, controller)
        activityWatcherForDialogs.alertDialog = mock(androidx.appcompat.app.AlertDialog::class.java)
        activityWatcherForDialogs.setupDialogCallback(WeakReference(mock(Activity::class.java)))

        activityWatcherForDialogs.dialogCallback?.emitDialogState(DialogState(Dialog.MODE_MEDIA_UPGRADE))

        assertNotNull(activityWatcherForDialogs.alertDialog)
    }
}
