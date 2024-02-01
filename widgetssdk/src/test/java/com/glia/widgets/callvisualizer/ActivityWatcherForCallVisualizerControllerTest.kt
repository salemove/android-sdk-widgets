package com.glia.widgets.callvisualizer

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.callvisualizer.CallVisualizerSupportActivity.Companion.PERMISSION_TYPE_TAG
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.core.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.engagement.State
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.notNull
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ActivityWatcherForCallVisualizerControllerTest {
    private val engagementEndFlow: PublishProcessor<State> = PublishProcessor.create()

    private val callVisualizerController: CallVisualizerController = mock {
        on { engagementStartFlow } doReturn Flowable.empty()
        on { engagementEndFlow } doReturn engagementEndFlow
    }
    private val screenSharingController = mock(ScreenSharingController::class.java)
    private val watcher = mock(ActivityWatcherForCallVisualizerContract.Watcher::class.java)
    private val isShowOverlayPermissionRequestDialogUseCase = mock(IsShowOverlayPermissionRequestDialogUseCase::class.java)
    private val confirmationDialogLinksUseCase = mock(ConfirmationDialogLinksUseCase::class.java)
    private val controller = ActivityWatcherForCallVisualizerController(
        callVisualizerController,
        screenSharingController,
        isShowOverlayPermissionRequestDialogUseCase,
        confirmationDialogLinksUseCase
    )
    private val activity = mock(AppCompatActivity::class.java)
    private val supportActivity = mock(CallVisualizerSupportActivity::class.java)

    @Before
    fun setup() {
        val window = mock(Window::class.java)
        val view = mock(View::class.java)
        whenever(activity.window).thenReturn(window)
        whenever(window.decorView).thenReturn(view)
        controller.setWatcher(watcher)
        verify(callVisualizerController).engagementStartFlow
        verify(callVisualizerController).engagementEndFlow
        cleanup()
        resetMocks()
    }

    @Test
    fun `addScreenSharingCallback screen sharing skipped when call or chat is active`() {
        whenever(callVisualizerController.isCallOrChatScreenActive(activity)).thenReturn(true)
        val spyController = spy(controller)
        spyController.addScreenSharingCallback(activity)
        verify(spyController, never()).setupScreenSharingViewCallback()

        resetMocks()
    }

    @Test
    fun `onActivityResumed will trigger dialog callback when not call or chat screen`() {
        val spyController = spy(controller)
        whenever(callVisualizerController.isCallOrChatScreenActive(activity)).thenReturn(false)
        spyController.onActivityResumed(activity)
        verify(watcher, never()).engagementStarted()
        verify(spyController, never()).addScreenSharingCallback(activity)
        verify(spyController, never()).setupScreenSharingViewCallback()
        verify(screenSharingController).onResume(eq(activity), notNull())
        verify(watcher).setupDialogCallback()

        resetMocks()
    }

    @Test
    fun `engagement end event will reset controller when triggered`() {
        engagementEndFlow.onNext(State.FinishedCallVisualizer)
        verify(watcher).removeDialogFromStack()
        verify(screenSharingController).removeViewCallback(anyOrNull())
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
    fun `addScreenSharingCallback error is shown when onScreenSharingError`() {
        whenever(callVisualizerController.isCallOrChatScreenActive(activity)).thenReturn(false)
        val intent = mock(Intent::class.java)
        whenever(supportActivity.intent).thenReturn(intent)
        whenever(intent.getParcelableExtra<PermissionType>(PERMISSION_TYPE_TAG)).thenReturn(ScreenSharing)
        controller.onActivityResumed(activity)
        controller.addScreenSharingCallback(activity)
        verify(callVisualizerController, times(2)).isCallOrChatScreenActive(any())

        verify(screenSharingController).setViewCallback(anyOrNull())
        verify(screenSharingController).onResume(eq(activity), notNull())
        verify(watcher).setupDialogCallback()
        cleanup()
        resetMocks()

        controller.screenSharingViewCallback?.onScreenSharingRequestError("message")
        verify(watcher).showToast("message")
    }

    @Test
    fun `onActivityResumed does not call permissions when not CallVisualizerSupportActivity`() {
        val nonCallVisualizerSupportActivity = mock(Activity::class.java)
        controller.onActivityResumed(nonCallVisualizerSupportActivity)
        resetMocks()
        verify(nonCallVisualizerSupportActivity, never()).getSystemService(any())
    }

    @Test
    fun `onActivityResumed start screen sharing when ScreenSharingController calls requestScreenSharingCallback`() {
        whenever(callVisualizerController.isCallOrChatScreenActive(activity)).thenReturn(false)
        val nonCallVisualizerSupportActivity = mock(Activity::class.java)
        controller.onActivityResumed(nonCallVisualizerSupportActivity)

        val argumentCaptor = argumentCaptor<Function0<Unit>>()
        verify(screenSharingController).onResume(eq(nonCallVisualizerSupportActivity), argumentCaptor.capture())
        argumentCaptor.firstValue.invoke()

        verify(screenSharingController).onScreenSharingAcceptedAndPermissionAsked(nonCallVisualizerSupportActivity)

        resetMocks()
    }

    @Test
    fun `onActivityPaused cleanup of callbacks`() {
        controller.onActivityPaused()
        verify(watcher).dismissAlertDialog()
        verify(watcher).removeDialogCallback()
        verify(screenSharingController, never()).removeViewCallback(anyOrNull())
    }

    @Test
    fun `isCallOrChatActive returns mocked value when called`() {
        whenever(callVisualizerController.isCallOrChatScreenActive(activity)).thenReturn(true)
        assertTrue(controller.isCallOrChatActive(activity))
        verify(callVisualizerController).isCallOrChatScreenActive(any())
    }

    @Test
    fun `onPositiveDialogButtonClicked dialog is dismissed and permission view is opened when MODE_OVERLAY_PERMISSION`() {
        controller.onDialogControllerCallback(DialogState.OverlayPermission)
        resetMocks()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).dismissOverlayDialog()
        verify(watcher).openOverlayPermissionView()
        verify(watcher).destroySupportActivityIfExists()
    }

    @Test
    fun `onPositiveDialogButtonClicked dialog is dismissed when MODE_OVERLAY_PERMISSION`() {
        controller.onDialogControllerCallback(DialogState.None)
        resetMocks()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).destroySupportActivityIfExists()
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_NONE`() {
        controller.onDialogControllerCallback(DialogState.None)
        resetMocks()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
    }

    @Test
    fun `onPositiveDialogButtonClicked notification channel dialog is shown when MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(true)
        controller.onDialogControllerCallback(DialogState.EnableScreenSharingNotificationsAndStartSharing)
        verify(watcher).showAllowScreenSharingNotificationsAndStartSharingDialog()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).openNotificationChannelScreen()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
        verify(watcher).destroySupportActivityIfExists()
    }

    @Test
    fun `onNegativeDialogButtonClicked decline is sent and dialog is dismissed when MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(true)
        controller.onDialogControllerCallback(DialogState.EnableScreenSharingNotificationsAndStartSharing)
        verify(watcher).showAllowScreenSharingNotificationsAndStartSharingDialog()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
        verify(watcher).removeDialogFromStack()
        verify(screenSharingController).onScreenSharingDeclined()
    }

    @Test
    fun `onPositiveDialogButtonClicked overlay permissions are called when MODE_OVERLAY_PERMISSIONS`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(true)
        controller.onDialogControllerCallback(DialogState.OverlayPermission)
        verify(watcher).showOverlayPermissionsDialog()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).dismissOverlayDialog()
        verify(watcher).removeDialogFromStack()
        verify(watcher).openOverlayPermissionView()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
        verify(watcher).destroySupportActivityIfExists()
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_OVERLAY_PERMISSIONS`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(true)
        controller.onDialogControllerCallback(DialogState.OverlayPermission)
        verify(watcher).showOverlayPermissionsDialog()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).dismissOverlayDialog()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
    }

    @Test
    fun `onPositiveDialogButtonClicked notification channel is shown when MODE_ENABLE_NOTIFICAIONTS_CHANNEL`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(true)
        controller.onDialogControllerCallback(DialogState.EnableNotificationChannel)
        verify(watcher).showAllowNotificationsDialog()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).openNotificationChannelScreen()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
        verify(watcher).destroySupportActivityIfExists()
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_ENABLE_NOTIFICATIONS_CHANNEL`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(true)
        controller.onDialogControllerCallback(DialogState.EnableNotificationChannel)
        verify(watcher).showAllowNotificationsDialog()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
    }

    @Test
    fun `onPositiveDialogButtonClicked openSupportActivity called when isSupportActivityOpen false and MODE_VISITOR_CODE`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(false)
        controller.onDialogControllerCallback(DialogState.VisitorCode)
        verify(watcher, never()).showVisitorCodeDialog()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
        verify(watcher).openSupportActivity(any())
        verify(watcher).destroySupportActivityIfExists()
    }

    @Test
    fun `onPositiveDialogButtonClicked dialog is dismissed when MODE_VISITOR_CODE`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(true)
        controller.onDialogControllerCallback(DialogState.VisitorCode)
        verify(watcher).showVisitorCodeDialog()
        controller.onPositiveDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
        verify(watcher).destroySupportActivityIfExists()
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_VISITOR_CODE`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(true)
        controller.onDialogControllerCallback(DialogState.VisitorCode)
        verify(watcher).showVisitorCodeDialog()
        controller.onNegativeDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
    }

    @Test
    fun `onPositiveDialogButtonClicked openSupportActivity called when isSupportActivityOpen false and MODE_SCREEN_SHARING`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(false)
        val operatorName = "Operator"
        controller.onDialogControllerCallback(DialogState.StartScreenSharing(operatorName))
        verify(watcher, never()).showScreenSharingDialog(any())
        controller.onPositiveDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
        verify(watcher).destroySupportActivityIfExists()
        verify(watcher).openSupportActivity(any())
    }

    @Test
    fun `onPositiveDialogButtonClicked dialog is dismissed when MODE_SCREEN_SHARING`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(true)
        val operatorName = "Operator"
        controller.onDialogControllerCallback(DialogState.StartScreenSharing(operatorName))
        verify(watcher).showScreenSharingDialog(eq(operatorName))
        controller.onPositiveDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
        verify(watcher).destroySupportActivityIfExists()
    }

    @Test
    fun `onNegativeDialogButtonClicked dialog is dismissed when MODE_SCREEN_SHARING`() {
        whenever(watcher.isSupportActivityOpen()).thenReturn(true)
        val operatorName = "Operator"
        controller.onDialogControllerCallback(DialogState.StartScreenSharing(operatorName))
        verify(watcher).showScreenSharingDialog(eq(operatorName))
        controller.onNegativeDialogButtonClicked()
        verify(watcher).removeDialogFromStack()
        verify(screenSharingController).onScreenSharingDeclined()
        verify(watcher).isWebBrowserActivityOpen()
        verify(watcher).isSupportActivityOpen()
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
    fun `onMediaProjectionPermissionResult accepts screen sharing when isGranted`() {
        controller.onMediaProjectionPermissionResult(isGranted = true, activity = activity)
        verify(screenSharingController).onScreenSharingAccepted(activity)
    }

    @Test
    fun `onMediaProjectionPermissionResult declines screen sharing when isGranted = false`() {
        controller.onMediaProjectionPermissionResult(isGranted = false, activity = activity)
        verify(screenSharingController).onScreenSharingDeclined()
    }

    private fun resetMocks() {
        reset(watcher, callVisualizerController, screenSharingController)
    }

    @After
    fun cleanup() {
        verifyNoMoreInteractions(
            watcher,
            callVisualizerController,
            screenSharingController,
            isShowOverlayPermissionRequestDialogUseCase
        )
    }
}
