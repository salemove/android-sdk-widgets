package com.glia.widgets.callvisualizer

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.callvisualizer.domain.IsGliaActivityUseCase
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class ActivityWatcherForDialogsTest {

    private lateinit var activityWatcherForDialogs: ActivityWatcherForDialogs

    @Before
    fun setUp() {
        val dialogController: DialogController = mock()
        val callVisualizerController = CallVisualizerController(
            mock(),
            dialogController,
            IsGliaActivityUseCase()
        )
        activityWatcherForDialogs = ActivityWatcherForDialogs(
            callVisualizerController,
            mock(),
            dialogController
        )
        activityWatcherForDialogs.alertDialog = mock()
        activityWatcherForDialogs.onActivityResumed(mock())
        activityWatcherForDialogs.setupDialogCallback()
    }

    @Test
    fun resumedActivity_cleared_whenActivityPaused() {
        activityWatcherForDialogs.onActivityResumed(mock())
        activityWatcherForDialogs.onActivityPaused(mock())

        assertNull(activityWatcherForDialogs.resumedActivity)
    }

    @Test
    fun resumedActivity_saved_whenActivityResumed() {
        activityWatcherForDialogs.onActivityResumed(mock())

        assertNotNull(activityWatcherForDialogs.resumedActivity)
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
    fun mediaProjectionObjects_null_whenGliaActivity() {
        val activity: ChatActivity = mock()
        activityWatcherForDialogs.onActivityPreCreated(activity, null)

        assertNull(activityWatcherForDialogs.startMediaProjection)
    }

    @Test
    fun mediaProjectionObjects_creation_whenComponentActivity() {
        val activity: ComponentActivity = mock()
        activityWatcherForDialogs.registerForMediaProjectionPermissionResult(activity)

        verify(
            activity,
            times(1)
        ).registerForActivityResult<ActivityResultContracts.StartActivityForResult, Intent>(
            any(),
            any()
        )
    }

    @Test
    fun mediaProjectionObjects_creation_whenComponentActivitySubclass() {
        val activity: FragmentActivity = mock()
        activityWatcherForDialogs.registerForMediaProjectionPermissionResult(activity)

        verify(
            activity,
            times(1)
        ).registerForActivityResult<ActivityResultContracts.StartActivityForResult, Intent>(
            any(),
            any()
        )
    }
}
