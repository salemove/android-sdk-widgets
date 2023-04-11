package com.glia.widgets.callvisualizer

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.core.dialog.Dialog.*
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.screensharing.ScreenSharingController
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import java.util.function.Consumer

internal class ActivityWatcherForCallVisualizerControllerTest {

    private val callVisualizerRepository = mock(CallVisualizerRepository::class.java)
    private val callVisualizerController = mock(CallVisualizerController::class.java)
    private val screenSharingController = mock(ScreenSharingController::class.java)
    private val isCallOrChatActiveUseCase = mock(IsCallOrChatScreenActiveUseCase::class.java)
    private val watcher = mock(ActivityWatcherForCallVisualizerContract.Watcher::class.java)
    private val isShowOverlayPermissionRequestDialogUseCase = mock(IsShowOverlayPermissionRequestDialogUseCase::class.java)
    private val controller = ActivityWatcherForCallVisualizerController(callVisualizerController, screenSharingController, isShowOverlayPermissionRequestDialogUseCase)
    private val activity = mock(AppCompatActivity::class.java)
    private val supportActivity = mock(CallVisualizerSupportActivity::class.java)
    private val mediaUpgradeOffer = mock(MediaUpgradeOffer::class.java)

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
        controller.onActivityResumed(activity)
        verify(callVisualizerController, times(2)).isCallOrChatScreenActiveUseCase
    }

    @Test
    fun `onScreenSharingStarted calls for a overlay dialog when isShowOverlayPermissionRequestDialogUseCase returns true`() {
        controller.setupScreenSharingViewCallback()
        whenever(isShowOverlayPermissionRequestDialogUseCase.execute()).thenReturn(true)
        controller.screenSharingViewCallback?.onScreenSharingStarted()
        verify(isShowOverlayPermissionRequestDialogUseCase).execute()
        verify(watcher).callOverlayDialog()
    }

    @Test
    fun `onActivityResumed error is shown when onScreenSharingError`() {
        mockOnActivityResumeAndEnsureCallbacksSet(supportActivity)
        controller.screenSharingViewCallback?.onScreenSharingRequestError(GliaException("message", GliaException.Cause.INTERNAL_ERROR))
        verify(watcher).showToast("message")
    }

    @Test
    fun `onActivityPaused cleanup of callbacks`() {
        controller.onActivityPaused()
        verify(watcher).dismissAlertDialog(false)
        verify(watcher).removeDialogCallback()
        verify(screenSharingController).removeViewCallback(anyOrNull())
    }

    @Test
    fun `isCallOrChatActive returns mocked value when called`() {
        whenever(callVisualizerController.isCallOrChatScreenActiveUseCase).thenReturn(isCallOrChatActiveUseCase)
        whenever(isCallOrChatActiveUseCase(activity)).thenReturn(true)
        assertTrue(controller.isCallOrChatActive(activity))
        verify(callVisualizerController).isCallOrChatScreenActiveUseCase
    }

    @Test
    fun `onPositiveDialogButtonClicked dialog is dismissed and permission view is opened when MODE_OVERLAY_PERMISSION`() {
        controller.onDialogControllerCallback(DialogState(MODE_OVERLAY_PERMISSION))
        resetMocks()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
        verify(watcher).dismissOverlayDialog()
        verify(watcher).openOverlayPermissionView()
    }

    @Test
    fun `onPositiveDialogButtonClicked dialog is dismissed when MODE_OVERLAY_PERMISSION`() {
        controller.onDialogControllerCallback(DialogState(MODE_NONE))
        resetMocks()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_NONE`() {
        controller.onDialogControllerCallback(DialogState(MODE_NONE))
        resetMocks()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
    }

    @Test
    fun `onPositiveDialogButtonClicked notification channel dialog is shown when MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING`() {
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
        verify(watcher).dismissOverlayDialog()
        verify(watcher).dismissAlertDialog(true)
        verify(watcher).openOverlayPermissionView()
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_OVERLAY_PERMISSIONS`() {
        controller.onDialogControllerCallback(DialogState(MODE_OVERLAY_PERMISSION))
        verify(watcher).showOverlayPermissionsDialog()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).dismissAlertDialog(true)
        verify(watcher).dismissOverlayDialog()
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

    @Test
    fun `onMediaUpgradeAccept does not destroy support activity when error occurs`() {
        controller.onMediaUpgradeReceived(mediaUpgradeOffer)
        resetMocks()
        controller.onMediaUpgradeAccept(GliaException("message", GliaException.Cause.INTERNAL_ERROR))
    }

    @Test
    fun `onMediaUpgradeAccept does destroy support activity when requestCameraPermission is true`() {
        controller.onMediaUpgradeReceived(mediaUpgradeOffer)
        resetMocks()
        controller.onMediaUpgradeAccept(null)
    }

    @Test
    fun `onMediaUpgradeDecline does cleanup when decline`() {
        controller.onMediaUpgradeReceived(mediaUpgradeOffer)
        resetMocks()
        controller.onMediaUpgradeDecline(null)
        verify(watcher).dismissAlertDialog(true)
    }

    @Test
    fun `onMediaUpgradeDecline does not destroy support activity when error occurs`() {
        controller.onMediaUpgradeReceived(mediaUpgradeOffer)
        resetMocks()
        controller.onMediaUpgradeDecline(GliaException("message", GliaException.Cause.INTERNAL_ERROR))
    }

    @Test
    fun `onInitialCameraPermissionResult accepts offer when permission is not needed`() {
        val mediaUpgradeOffer = dummyMediaVideoUpgradeOffer(videoDirection = MediaDirection.ONE_WAY)
        controller.mediaUpgradeOffer = mediaUpgradeOffer
        resetMocks()
        controller.onInitialCameraPermissionResult(isGranted = true, isComponentActivity = true)
        verify(mediaUpgradeOffer).accept(notNull())
    }

    @Test
    fun `onInitialCameraPermissionResult accepts offer when permission is granted and permissionType = CAMERA`() {
        val mediaUpgradeOffer = dummyMediaVideoUpgradeOffer(videoDirection = MediaDirection.TWO_WAY)
        controller.onMediaUpgradeReceived(mediaUpgradeOffer)
        resetMocks()
        controller.onInitialCameraPermissionResult(isGranted = true, isComponentActivity = true)
    }

    @Test
    fun `onInitialCameraPermissionResult requests permission when permission is not granted and is ComponentActivity`() {
        val mediaUpgradeOffer = dummyMediaVideoUpgradeOffer(videoDirection = MediaDirection.TWO_WAY)
        controller.onMediaUpgradeReceived(mediaUpgradeOffer)
        resetMocks()
        controller.onInitialCameraPermissionResult(isGranted = false, isComponentActivity = true)
        verify(watcher).requestCameraPermission()
    }

    @Test
    fun `onInitialCameraPermissionResult opens ComponentActivity when permission is not granted and is not ComponentActivity`() {
        val mediaUpgradeOffer = dummyMediaVideoUpgradeOffer(videoDirection = MediaDirection.TWO_WAY)
        controller.onMediaUpgradeReceived(mediaUpgradeOffer)
        resetMocks()
        controller.onInitialCameraPermissionResult(isGranted = false, isComponentActivity = false)
        verify(watcher).openComponentActivity()
    }

    @Test
    fun `onRequestedCameraPermissionResult accepts when isGranted`() {
        val mediaUpgradeOffer = dummyMediaVideoUpgradeOffer(videoDirection = MediaDirection.TWO_WAY)
        controller.onMediaUpgradeReceived(mediaUpgradeOffer)
        resetMocks()
        controller.onRequestedCameraPermissionResult(true)
        verify(watcher).destroySupportActivityIfExists()
        verify(mediaUpgradeOffer).accept(notNull())
    }

    @Test
    fun `onRequestedCameraPermissionResult declines when !isGranted`() {
        val mediaUpgradeOffer = dummyMediaVideoUpgradeOffer(videoDirection = MediaDirection.TWO_WAY)
        controller.onMediaUpgradeReceived(mediaUpgradeOffer)
        resetMocks()
        controller.onRequestedCameraPermissionResult(false)
        verify(watcher).destroySupportActivityIfExists()
        verify(mediaUpgradeOffer).decline(notNull())
    }

    @Test
    fun `onMediaProjectionPermissionResult accepts screen sharing when isGranted`() {
        controller.onMediaProjectionPermissionResult(isGranted = true, context = activity)
        verify(screenSharingController).onScreenSharingAccepted(activity)
    }

    @Test
    fun `onMediaProjectionPermissionResult declines screen sharing when isGranted = false`() {
        controller.onMediaProjectionPermissionResult(isGranted = false, context = activity)
        verify(screenSharingController).onScreenSharingDeclined()
    }

    private fun prepareMediaUpgradeApplicationState() {
        val offer = mock(MediaUpgradeOffer::class.java)
        val state = DialogState.MediaUpgrade(offer, "name", MODE_MEDIA_UPGRADE)
        controller.onDialogControllerCallback(state)
        verify(watcher).showUpgradeDialog(state)
    }

    private fun mockOnActivityResumeAndEnsureCallbacksSet(activity: Activity) {
        whenever(callVisualizerController.isCallOrChatScreenActiveUseCase).thenReturn(isCallOrChatActiveUseCase)
        whenever(isCallOrChatActiveUseCase(activity)).thenReturn(false)
        controller.onInitialCameraPermissionResult(
            isGranted = false,
            isComponentActivity = true
        )
        controller.onActivityResumed(activity)
        verify(callVisualizerController, times(2)).isCallOrChatScreenActiveUseCase
        verify(screenSharingController).setViewCallback(anyOrNull())
        verify(screenSharingController).onResume(activity)
        verify(watcher).setupDialogCallback()
        verify(watcher).requestCameraPermission()
        cleanup()
        resetMocks()
    }

    // Required because MediaUpgradeOffer has 'final' fields which Mockito can't mock
    private fun dummyMediaVideoUpgradeOffer(
        audioDirection: MediaDirection = MediaDirection.NONE,
        videoDirection: MediaDirection = MediaDirection.NONE
    ): MediaUpgradeOffer {
        return spy(object: MediaUpgradeOffer(audioDirection, videoDirection) {
            override fun accept(callback: Consumer<GliaException?>?) {
                // Do nothing
            }

            override fun decline(callback: Consumer<GliaException?>?) {
                // Do nothing
            }
        })
    }

    private fun resetMocks() {
        reset(watcher, callVisualizerController, callVisualizerRepository, screenSharingController)
    }

    @After
    fun cleanup() {
        verifyNoMoreInteractions(
            watcher,
            callVisualizerController,
            callVisualizerRepository,
            screenSharingController,
            isShowOverlayPermissionRequestDialogUseCase
        )
    }
}
