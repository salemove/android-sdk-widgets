package com.glia.widgets.callvisualizer

import android.app.Activity
import android.view.Window
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.glia.widgets.base.GliaActivity
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.callvisualizer.domain.IsGliaActivityUseCase
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ActivityWatcherForCallVisualizerTest {

    private lateinit var activityWatcherForCallVisualizer: ActivityWatcherForCallVisualizer
    private val mockActivity: Activity by lazy {
        val activity: Activity = mock()
        val window: Window = mock()
        whenever(activity.window).thenReturn(window)
        whenever(window.decorView).thenReturn(mock())
        activity
    }

    @Before
    fun setUp() {
        val dialogController: DialogController = mock()
        val callVisualizerController = CallVisualizerController(
            mock(),
            dialogController,
            IsGliaActivityUseCase()
        )
        activityWatcherForCallVisualizer = ActivityWatcherForCallVisualizer(
            callVisualizerController,
            mock(),
            dialogController,
            mock()
        )
        activityWatcherForCallVisualizer.alertDialog = mock()
        activityWatcherForCallVisualizer.onActivityResumed(mockActivity)
        activityWatcherForCallVisualizer.setupDialogCallback()
    }

    @Test
    fun resumedActivity_cleared_whenActivityPaused() {
        activityWatcherForCallVisualizer.onActivityResumed(mockActivity)
        activityWatcherForCallVisualizer.onActivityPaused(mockActivity)
        whenever(activityWatcherForCallVisualizer.getGliaViewOrRootView(mockActivity)).thenReturn(mock())

        assertNull(activityWatcherForCallVisualizer.resumedActivity)
    }

    @Test
    fun resumedActivity_saved_whenActivityResumed() {
        activityWatcherForCallVisualizer.onActivityResumed(mockActivity)

        assertNotNull(activityWatcherForCallVisualizer.resumedActivity)
    }

    @Test
    fun alertDialog_dismissed_whenEmitDialogStateModeNone() {
        activityWatcherForCallVisualizer.dialogCallback?.emitDialogState(DialogState(Dialog.MODE_NONE))


        assertNull(activityWatcherForCallVisualizer.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeMediaUpgrade() {
        val state = DialogState(Dialog.MODE_MEDIA_UPGRADE)
        activityWatcherForCallVisualizer.dialogCallback?.emitDialogState(state)
        assertNotNull(activityWatcherForCallVisualizer.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeOverlayPermission() {
        val state = DialogState(Dialog.MODE_OVERLAY_PERMISSION)
        activityWatcherForCallVisualizer.dialogCallback?.emitDialogState(state)
        assertNotNull(activityWatcherForCallVisualizer.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeStartScreenSharing() {
        val state = DialogState(Dialog.MODE_START_SCREEN_SHARING)
        activityWatcherForCallVisualizer.dialogCallback?.emitDialogState(state)
        assertNotNull(activityWatcherForCallVisualizer.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeEnableNotifications() {
        val state = DialogState(Dialog.MODE_ENABLE_NOTIFICATION_CHANNEL)
        activityWatcherForCallVisualizer.dialogCallback?.emitDialogState(state)
        assertNotNull(activityWatcherForCallVisualizer.alertDialog)
    }

    @Test
    fun alertDialog_created_whenEmitDialogStateModeNotificationsAndScreenSharing() {
        val state = DialogState(Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING)
        activityWatcherForCallVisualizer.dialogCallback?.emitDialogState(state)
        assertNotNull(activityWatcherForCallVisualizer.alertDialog)
    }

    @Test
    fun mediaProjectionObjects_null_whenGliaActivity() {
        val activity: Activity = mock(extraInterfaces = arrayOf(GliaActivity::class))
        activityWatcherForCallVisualizer.onActivityPreCreated(activity, null)

        assertTrue(activityWatcherForCallVisualizer.startMediaProjectionLaunchers.isEmpty())
    }

    @Test
    fun mediaProjectionObjects_creation_whenComponentActivity() {
        val activity: ComponentActivity = mock()
        activityWatcherForCallVisualizer.registerForMediaProjectionPermissionResult(activity)

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
        activityWatcherForCallVisualizer.registerForMediaProjectionPermissionResult(activity)

        verify(
            activity,
            times(1)
        ).registerForActivityResult<ActivityResultContracts.StartActivityForResult, Intent>(
            any(),
            any()
        )
    }
}
