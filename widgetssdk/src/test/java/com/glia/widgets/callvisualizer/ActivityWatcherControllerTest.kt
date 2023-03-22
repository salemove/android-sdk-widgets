package com.glia.widgets.callvisualizer

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.core.dialog.Dialog.*
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.view.head.controller.ServiceChatHeadController
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import java.lang.ref.WeakReference

internal class ActivityWatcherControllerTest {

    private val callVisualizerRepository = mock(CallVisualizerRepository::class.java)
    private val callVisualizerController = mock(CallVisualizerController::class.java)
    private val serviceChatHeadController = mock(ServiceChatHeadController::class.java)
    private val screenSharingController = mock(ScreenSharingController::class.java)
    private val isCallOrChatActiveUseCase = mock(IsCallOrChatScreenActiveUseCase::class.java)
    private val watcher = mock(ActivityWatcherContract.Watcher::class.java)
    private val controller = ActivityWatcherController(callVisualizerController, screenSharingController, serviceChatHeadController)
    private val activity = mock(Activity::class.java)
    private val activityReference: WeakReference<Activity?> = WeakReference(activity)
    private val view = mock(View::class.java)

    @Before
    fun setup() {
        val window = mock(Window::class.java)
        val view = mock(View::class.java)
        whenever(activity.window).thenReturn(window)
        whenever(window.decorView).thenReturn(view)
        controller.setWatcher(watcher)
        cleanup()
        resetMocks()
    }

    @Test
    fun `onActivityResumed screen sharing skipped when call or chat is active`() {
        whenever(callVisualizerController.isCallOrChatScreenActiveUseCase).thenReturn(isCallOrChatActiveUseCase)
        whenever(isCallOrChatActiveUseCase(activity)).thenReturn(true)
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.onActivityResumed(activity)
        verify(watcher).fetchGliaOrRootView()
        verify(callVisualizerController, times(2)).isCallOrChatScreenActiveUseCase
        verify(serviceChatHeadController).onResume(view)
    }

    @Test
    fun `onActivityResumed bubble is resumed when onScreenSharingStarted`() {
        `onActivityResumed callbacks are set when call or chat are not active`()
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.screenSharingViewCallback?.onScreenSharingStarted()
        verify(watcher).fetchGliaOrRootView()
        verify(serviceChatHeadController).onResume(view)
    }

    @Test
    fun `onActivityResumed error is shown when onScreenSharingError`() {
        `onActivityResumed callbacks are set when call or chat are not active`()
        controller.screenSharingViewCallback?.onScreenSharingRequestError(GliaException("message", GliaException.Cause.INTERNAL_ERROR))
        verify(watcher).showToast("message")
    }

    @Test
    fun `onActivityPaused cleanup of callbacks`() {
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.onActivityPaused()
        verify(watcher).dismissAlertDialog(false)
        verify(watcher).removeDialogCallback()
        verify(watcher).fetchGliaOrRootView()
        verify(screenSharingController).removeViewCallback(anyOrNull())
        verify(serviceChatHeadController).onPause(view)
    }

    @Test
    fun `onActivityDestroyed bubble is destroyed`() {
        controller.onActivityDestroyed()
        verify(serviceChatHeadController).onDestroy()
    }

    @Test
    fun `isCallOrChatActive returns mocked value when called`() {
        whenever(callVisualizerController.isCallOrChatScreenActiveUseCase).thenReturn(isCallOrChatActiveUseCase)
        whenever(isCallOrChatActiveUseCase(activity)).thenReturn(true)
        assertTrue(controller.isCallOrChatActive(activity))
        verify(callVisualizerController).isCallOrChatScreenActiveUseCase
    }

    @Test
    fun `onPositiveDialogButtonClicked dialog is dismissed when MODE_NONE`() {
        controller.onDialogControllerCallback(DialogState(MODE_NONE))
        controller.onPositiveDialogButtonClicked()
        verify(watcher, times(2)).dismissAlertDialog(true)
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_NONE`() {
        controller.onDialogControllerCallback(DialogState(MODE_NONE))
        controller.onNegativeDialogButtonClicked()
        verify(watcher, times(2)).dismissAlertDialog(true)
    }

    @Test
    fun `onPositiveDialogButtonClicked screen sharing dialog is shown when MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING`() {
        controller.onDialogControllerCallback(DialogState(MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING))
        verify(watcher).showAllowScreenSharingNotificationsAndStartSharingDialog()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
        verify(watcher).openNotificationChannelScreen()
    }

    @Test
    fun `onNegativeDialogButtonClicked decline is sent and dialog is dismissed when MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING`() {
        controller.onDialogControllerCallback(DialogState(MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING))
        verify(watcher).showAllowScreenSharingNotificationsAndStartSharingDialog()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
        verify(screenSharingController).onScreenSharingDeclined()
    }

    @Test
    fun `onPositiveDialogButtonClicked call activity is called when MODE_MEDIA_UPGRADE`() {
        prepareMediaUpgradeApplicationState()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
        verify(watcher).openCallActivity()
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_MEDIA_UPGRADE`() {
        prepareMediaUpgradeApplicationState()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
    }

    @Test
    fun `onPositiveDialogButtonClicked overlay permissions are called when MODE_OVERLAY_PERMISSIONS`() {
        controller.onDialogControllerCallback(DialogState(MODE_OVERLAY_PERMISSION))
        verify(watcher).showOverlayPermissionsDialog()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
        verify(watcher).openOverlayPermissionView()
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_OVERLAY_PERMISSIONS`() {
        controller.onDialogControllerCallback(DialogState(MODE_OVERLAY_PERMISSION))
        verify(watcher).showOverlayPermissionsDialog()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
    }

    @Test
    fun `onPositiveDialogButtonClicked notification channel is shown when MODE_ENABLE_NOTIFICAIONTS_CHANNEL`() {
        controller.onDialogControllerCallback(DialogState(MODE_ENABLE_NOTIFICATION_CHANNEL))
        verify(watcher).showAllowNotificationsDialog()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
        verify(watcher).openNotificationChannelScreen()
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_ENABLE_NOTIFICATIONS_CHANNEL`() {
        controller.onDialogControllerCallback(DialogState(MODE_ENABLE_NOTIFICATION_CHANNEL))
        verify(watcher).showAllowNotificationsDialog()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
    }

    @Test
    fun `onPositiveDialogButtonClicked dialog is dismissed when MODE_VISITOR_CODE`() {
        controller.onDialogControllerCallback(DialogState(MODE_VISITOR_CODE))
        verify(watcher).showVisitorCodeDialog()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_VISITOR_CODE`() {
        controller.onDialogControllerCallback(DialogState(MODE_VISITOR_CODE))
        verify(watcher).showVisitorCodeDialog()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
    }

    @Test
    fun `onPositiveDialogButtonClicked dialog is dismissed when MODE_SCREEN_SHARING`() {
        controller.onDialogControllerCallback(DialogState(MODE_START_SCREEN_SHARING))
        verify(watcher).showScreenSharingDialog()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_SCREEN_SHARING`() {
        controller.onDialogControllerCallback(DialogState(MODE_START_SCREEN_SHARING))
        verify(watcher).showScreenSharingDialog()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
        verify(screenSharingController).onScreenSharingDeclined()
    }

    @Test
    fun `startMediaProjectionLaunchers map interactions with add and remove`() {
        val launcher: ActivityResultLauncher<Intent> = mock()
        assertTrue(controller.startMediaProjectionLaunchers.isEmpty())
        controller.startMediaProjectionLaunchers("name", launcher)
        assertTrue(controller.startMediaProjectionLaunchers.keys.contains("name"))
        controller.removeMediaProjectionLaunchers("name2")
        assertTrue(controller.startMediaProjectionLaunchers.keys.contains("name"))
        controller.removeMediaProjectionLaunchers("name")
        assertTrue(controller.startMediaProjectionLaunchers.isEmpty())
    }

    private fun prepareMediaUpgradeApplicationState() {
        val offer = mock(MediaUpgradeOffer::class.java)
        val state = DialogState.MediaUpgrade(offer, "name", MODE_MEDIA_UPGRADE)
        controller.onDialogControllerCallback(state)
        verify(watcher).showUpgradeDialog(state)
    }

    private fun `onActivityResumed callbacks are set when call or chat are not active`() {
        whenever(callVisualizerController.isCallOrChatScreenActiveUseCase).thenReturn(isCallOrChatActiveUseCase)
        whenever(isCallOrChatActiveUseCase(activity)).thenReturn(false)
        whenever(watcher.fetchGliaOrRootView()).thenReturn(view)
        controller.onActivityResumed(activity)
        verify(callVisualizerController, times(2)).isCallOrChatScreenActiveUseCase
        verify(serviceChatHeadController).onResume(view)
        verify(screenSharingController).setViewCallback(anyOrNull())
        verify(screenSharingController).onResume(activity)
        verify(watcher).setupDialogCallback()
        verify(watcher).fetchGliaOrRootView()
        cleanup()
        resetMocks()
    }

    private fun resetMocks() {
        reset(watcher, callVisualizerController, callVisualizerRepository, serviceChatHeadController, screenSharingController)
    }

    @After
    fun cleanup() {
        verifyNoMoreInteractions(watcher, callVisualizerController, callVisualizerRepository, serviceChatHeadController, screenSharingController)
    }
}