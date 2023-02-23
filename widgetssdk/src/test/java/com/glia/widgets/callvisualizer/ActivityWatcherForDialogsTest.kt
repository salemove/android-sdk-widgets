package com.glia.widgets.callvisualizer

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.FragmentActivity
import com.glia.widgets.call.CallActivity
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.screensharing.ScreenSharingController
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify
import java.lang.ref.WeakReference

internal class ActivityWatcherForDialogsTest {

    private lateinit var activityWatcherForDialogs: ActivityWatcherForDialogs

    @Before
    fun setUp() {
        val callVisualizerRepository = mock(CallVisualizerRepository::class.java)
        val dialogController = mock(DialogController::class.java)
        val callVisualizerController = CallVisualizerController(
            callVisualizerRepository,
            dialogController,
            IsCallOrChatScreenActiveUseCase()
        )
        val screenSharingController = mock(ScreenSharingController::class.java)
        activityWatcherForDialogs = ActivityWatcherForDialogs(
            callVisualizerController,
            screenSharingController,
            dialogController
        )
        activityWatcherForDialogs.alertDialog = mock(androidx.appcompat.app.AlertDialog::class.java)
        activityWatcherForDialogs.setupDialogCallback(WeakReference(mock(Activity::class.java)))
    }

    @Test
    fun resumedActivity_cleared_whenActivityPaused() {
        activityWatcherForDialogs.onActivityResumed(mock(Activity::class.java))
        activityWatcherForDialogs.onActivityPaused(mock(Activity::class.java))

        assertNull(activityWatcherForDialogs.resumedActivity.get())
    }

    @Test
    fun resumedActivity_saved_whenActivityResumed() {
        activityWatcherForDialogs.onActivityResumed(mock(Activity::class.java))

        assertNotNull(activityWatcherForDialogs.resumedActivity.get())
    }

    @Test
    fun alertDialog_dismissed_whenEmitDialogStateModeNone() {
        activityWatcherForDialogs.dialogCallback?.emitDialogState(DialogState(Dialog.MODE_NONE))

        assertNull(activityWatcherForDialogs.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeMediaUpgrade() {
        val state = DialogState(Dialog.MODE_MEDIA_UPGRADE)
        activityWatcherForDialogs.dialogCallback?.emitDialogState(state)
        assertNotNull(activityWatcherForDialogs.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeOverlayPermission() {
        val state = DialogState(Dialog.MODE_OVERLAY_PERMISSION)
        activityWatcherForDialogs.dialogCallback?.emitDialogState(state)
        assertNotNull(activityWatcherForDialogs.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeStartScreenSharing() {
        val state = DialogState(Dialog.MODE_START_SCREEN_SHARING)
        activityWatcherForDialogs.dialogCallback?.emitDialogState(state)
        assertNotNull(activityWatcherForDialogs.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeEnableNotifications() {
        val state = DialogState(Dialog.MODE_ENABLE_NOTIFICATION_CHANNEL)
        activityWatcherForDialogs.dialogCallback?.emitDialogState(state)
        assertNotNull(activityWatcherForDialogs.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeNotificationsAndScreenSharing() {
        val state = DialogState(Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING)
        activityWatcherForDialogs.dialogCallback?.emitDialogState(state)
        assertNotNull(activityWatcherForDialogs.alertDialog)
    }

    @Test
    fun mediaProjectionObjects_null_whenChatActivity() {
        val activity = mock(ChatActivity::class.java)
        activityWatcherForDialogs.onActivityPreCreated(activity, null)

        assertNull(activityWatcherForDialogs.startMediaProjection)
    }

    @Test
    fun mediaProjectionObjects_null_whenCallActivity() {
        val activity = mock(CallActivity::class.java)
        activityWatcherForDialogs.onActivityPreCreated(activity, null)

        assertNull(activityWatcherForDialogs.startMediaProjection)
    }

    @Test
    fun mediaProjectionObjects_creation_whenComponentActivity() {
        val activity = mock(ComponentActivity::class.java)
        activityWatcherForDialogs.registerForMediaProjectionPermissionResult(activity)

        verify(activity, times(1)).registerForActivityResult(
            any(ActivityResultContract::class.java),
            any()
        )
    }

    @Test
    fun mediaProjectionObjects_creation_whenComponentActivitySubclass() {
        val activity = mock(FragmentActivity::class.java)
        activityWatcherForDialogs.registerForMediaProjectionPermissionResult(activity)

        verify(activity, times(1)).registerForActivityResult(
            any(ActivityResultContract::class.java),
            any()
        )
    }
}
