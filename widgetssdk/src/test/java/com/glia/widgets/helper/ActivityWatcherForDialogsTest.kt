package com.glia.widgets.helper

import android.app.Activity
import android.app.Application
import com.glia.widgets.callvisualizer.CallVisualizerRepository
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.Dialog.MODE_NONE
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.properties.Delegates

internal class ActivityWatcherForDialogsTest {
    private var activity: Activity by Delegates.notNull()
    private var app: Application by Delegates.notNull()
    private var callVisualizerRepository: CallVisualizerRepository by Delegates.notNull()
    private var dialogController: DialogController by Delegates.notNull()
    private var controller: CallVisualizerController by Delegates.notNull()
    private var activityWatcherForDialogs: ActivityWatcherForDialogs by Delegates.notNull()

    @Before
    fun setUp() {
        activity = mock()
        app = mock()
        callVisualizerRepository = mock()
        dialogController = mock()
        controller = CallVisualizerController(
            callVisualizerRepository, dialogController, mock()
        )
        activityWatcherForDialogs = spy(ActivityWatcherForDialogs(app, controller))
    }

    @Test
    fun resumedActivity_cleared_whenActivityPaused() {
        activityWatcherForDialogs.onActivityResumed(activity)
        activityWatcherForDialogs.onActivityPaused(activity)

        assertNull(activityWatcherForDialogs.resumedActivity)
    }

    @Test
    fun resumedActivity_saved_whenActivityResumed() {
        activityWatcherForDialogs.onActivityResumed(activity)
        assertNotNull(activityWatcherForDialogs.resumedActivity)
    }

    @Test
    fun alertDialog_dismissed_whenEmitDialogStateModeNone() {
        activityWatcherForDialogs.onActivityResumed(activity)

        activityWatcherForDialogs.dialogCallback?.emitDialogState(DialogState(MODE_NONE))

        assertNull(activityWatcherForDialogs.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeMediaUpgrade() {
        activityWatcherForDialogs.alertDialog = mock()
        activityWatcherForDialogs.setupDialogCallback()

        activityWatcherForDialogs.dialogCallback?.emitDialogState(DialogState(Dialog.MODE_MEDIA_UPGRADE))

        assertNotNull(activityWatcherForDialogs.alertDialog)
    }
}
