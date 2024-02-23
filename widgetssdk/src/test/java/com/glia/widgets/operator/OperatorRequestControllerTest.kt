package com.glia.widgets.operator

import android.COMMON_EXTENSIONS_CLASS_PATH
import android.LOGGER_PATH
import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.core.notification.domain.ShowScreenSharingNotificationUseCase
import com.glia.widgets.core.permissions.domain.HasScreenSharingNotificationChannelEnabledUseCase
import com.glia.widgets.engagement.ScreenSharingState
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.CheckMediaUpgradePermissionsUseCase
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.engagement.domain.OperatorMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.isAudio
import com.glia.widgets.view.dialog.holder.DialogHolderActivity
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class OperatorRequestControllerTest {
    private val mediaUpgradeRequest = PublishProcessor.create<MediaUpgradeOfferData>()
    private val acceptMediaUpgradeRequestResult = PublishProcessor.create<MediaUpgradeOffer>()
    private val screenSharingProcessor = PublishProcessor.create<ScreenSharingState>()

    private lateinit var operatorMediaUpgradeOfferUseCase: OperatorMediaUpgradeOfferUseCase
    private lateinit var acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase
    private lateinit var declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase
    private lateinit var checkMediaUpgradePermissionsUseCase: CheckMediaUpgradePermissionsUseCase
    private lateinit var screenSharingUseCase: ScreenSharingUseCase
    private lateinit var hasScreenSharingNotificationChannelEnabledUseCase: HasScreenSharingNotificationChannelEnabledUseCase
    private lateinit var currentOperatorUseCase: CurrentOperatorUseCase
    private lateinit var showScreenSharingNotificationUseCase: ShowScreenSharingNotificationUseCase
    private lateinit var removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase
    private lateinit var isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase
    private lateinit var isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase
    private lateinit var setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase
    private lateinit var dialogController: DialogContract.Controller
    private lateinit var dialogCallbackSlot: CapturingSlot<DialogContract.Controller.Callback>
    private lateinit var gliaSdkConfigurationManager: GliaSdkConfigurationManager

    private lateinit var controller: OperatorRequestContract.Controller

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        operatorMediaUpgradeOfferUseCase = mockk(relaxUnitFun = true)
        every { operatorMediaUpgradeOfferUseCase() } returns mediaUpgradeRequest

        acceptMediaUpgradeOfferUseCase = mockk(relaxUnitFun = true) {
            every { result } returns acceptMediaUpgradeRequestResult
        }
        declineMediaUpgradeOfferUseCase = mockk(relaxUnitFun = true)
        checkMediaUpgradePermissionsUseCase = mockk(relaxUnitFun = true)
        screenSharingUseCase = mockk(relaxUnitFun = true)
        every { screenSharingUseCase() } returns screenSharingProcessor
        hasScreenSharingNotificationChannelEnabledUseCase = mockk(relaxUnitFun = true)
        currentOperatorUseCase = mockk(relaxUnitFun = true)
        showScreenSharingNotificationUseCase = mockk(relaxUnitFun = true)
        removeScreenSharingNotificationUseCase = mockk(relaxUnitFun = true)
        isShowOverlayPermissionRequestDialogUseCase = mockk(relaxUnitFun = true)
        isCurrentEngagementCallVisualizerUseCase = mockk(relaxUnitFun = true)
        setOverlayPermissionRequestDialogShownUseCase = mockk(relaxUnitFun = true)
        dialogController = mockk(relaxUnitFun = true)

        dialogCallbackSlot = slot()

        gliaSdkConfigurationManager = mockk(relaxed = true)

        controller = OperatorRequestController(
            operatorMediaUpgradeOfferUseCase,
            acceptMediaUpgradeOfferUseCase,
            declineMediaUpgradeOfferUseCase,
            checkMediaUpgradePermissionsUseCase,
            screenSharingUseCase,
            hasScreenSharingNotificationChannelEnabledUseCase,
            currentOperatorUseCase,
            showScreenSharingNotificationUseCase,
            removeScreenSharingNotificationUseCase,
            isShowOverlayPermissionRequestDialogUseCase,
            isCurrentEngagementCallVisualizerUseCase,
            setOverlayPermissionRequestDialogShownUseCase,
            dialogController,
            gliaSdkConfigurationManager
        )

        verify { operatorMediaUpgradeOfferUseCase() }
        verify { acceptMediaUpgradeOfferUseCase.result }
        verify { dialogController.addCallback(capture(dialogCallbackSlot)) }
        verify { screenSharingUseCase() }
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
        confirmVerified(
            operatorMediaUpgradeOfferUseCase,
            acceptMediaUpgradeOfferUseCase,
            declineMediaUpgradeOfferUseCase,
            checkMediaUpgradePermissionsUseCase,
            dialogController
        )
    }

    @Test
    fun `MediaUpgrade offer will trigger upgrade dialog`() {
        val data: MediaUpgradeOfferData = mockk()
        mediaUpgradeRequest.onNext(data)
        verify { dialogController.showUpgradeDialog(data) }
    }

    @Test
    fun `handleMediaUpgradeOfferAcceptResult will trigger OpenCallActivity state with Audio media type when offer is for Audio`() {
        val offer: MediaUpgradeOffer = mockk()
        mockkStatic(COMMON_EXTENSIONS_CLASS_PATH)

        every { any<MediaUpgradeOffer>().isAudio } returns true

        val state = controller.state.test()
        acceptMediaUpgradeRequestResult.onNext(offer)

        verify { offer.isAudio }

        confirmVerified(offer)
        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestContract.State.OpenCallActivity(Engagement.MediaType.AUDIO)))

        unmockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `handleMediaUpgradeOfferAcceptResult will trigger OpenCallActivity state with Video media type when offer is for Video`() {
        val offer: MediaUpgradeOffer = mockk()
        mockkStatic(COMMON_EXTENSIONS_CLASS_PATH)

        every { any<MediaUpgradeOffer>().isAudio } returns false

        val state = controller.state.test()
        acceptMediaUpgradeRequestResult.onNext(offer)

        verify { offer.isAudio }

        confirmVerified(offer)
        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestContract.State.OpenCallActivity(Engagement.MediaType.VIDEO)))

        unmockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `handleDialogCallback will produce RequestMediaUpgrade state when MediaUpgrade dialog state is triggered`() {
        val data: MediaUpgradeOfferData = mockk()
        val state = controller.state.test()

        dialogCallbackSlot.captured.emitDialogState(DialogState.MediaUpgrade(data))

        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestContract.State.RequestMediaUpgrade(data)))
    }

    @Test
    fun `handleDialogCallback will produce DismissAlertDialog state when None dialog state is triggered`() {
        val state = controller.state.test()

        dialogCallbackSlot.captured.emitDialogState(DialogState.None)

        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestContract.State.DismissAlertDialog))
    }

    @Test
    fun `handleDialogCallback will produce EnableScreenSharingNotificationsAndStartSharing state when EnableScreenSharingNotificationsAndStartSharing dialog state is triggered`() {
        val state = controller.state.test()

        dialogCallbackSlot.captured.emitDialogState(DialogState.EnableScreenSharingNotificationsAndStartSharing)

        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestContract.State.EnableScreenSharingNotificationsAndStartSharing))
    }

    @Test
    fun `handleDialogCallback will produce ShowScreenSharingDialog state when StartScreenSharing dialog state is triggered`() {
        val state = controller.state.test()

        val operatorName = "Operator"
        every { currentOperatorUseCase.formattedNameValue } returns operatorName

        dialogCallbackSlot.captured.emitDialogState(DialogState.StartScreenSharing)
        verify { currentOperatorUseCase.formattedNameValue }

        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestContract.State.ShowScreenSharingDialog(operatorName)))
    }

    @Test
    fun `handleDialogCallback will produce ShowOverlayDialog state when CVOverlayPermission dialog state is triggered`() {
        val state = controller.state.test()

        dialogCallbackSlot.captured.emitDialogState(DialogState.CVOverlayPermission)

        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestContract.State.ShowOverlayDialog))
    }

    @Test
    fun `onMediaUpgradeAccepted will accept media upgrade when necessary permissions are granted`() {
        val permissionCallbackSlot: CapturingSlot<(Boolean) -> Unit> = slot()
        val activity: ChatActivity = mockk(relaxUnitFun = true)
        val offer: MediaUpgradeOffer = mockk()

        controller.onMediaUpgradeAccepted(offer, activity)
        verify { dialogController.dismissCurrentDialog() }
        verify { checkMediaUpgradePermissionsUseCase(offer, capture(permissionCallbackSlot)) }

        permissionCallbackSlot.captured(true)
        verify(exactly = 0) { activity.finish() }
        verify { acceptMediaUpgradeOfferUseCase(offer) }
        verify(exactly = 0) { declineMediaUpgradeOfferUseCase(offer) }

        confirmVerified(activity, offer)
    }

    @Test
    fun `onMediaUpgradeAccepted will decline media upgrade when necessary permissions are not granted`() {
        val permissionCallbackSlot: CapturingSlot<(Boolean) -> Unit> = slot()
        val activity: DialogHolderActivity = mockk(relaxUnitFun = true)
        val offer: MediaUpgradeOffer = mockk()

        controller.onMediaUpgradeAccepted(offer, activity)
        verify { dialogController.dismissCurrentDialog() }
        verify { checkMediaUpgradePermissionsUseCase(offer, capture(permissionCallbackSlot)) }

        permissionCallbackSlot.captured(false)
        verify { activity.finish() }
        verify(exactly = 0) { acceptMediaUpgradeOfferUseCase(offer) }
        verify { declineMediaUpgradeOfferUseCase(offer) }

        confirmVerified(activity, offer)

    }

    @Test
    fun `onMediaUpgradeDeclined will decline media upgrade and finish activity when it is DialogHolder`() {
        val activity: DialogHolderActivity = mockk(relaxUnitFun = true)
        val offer: MediaUpgradeOffer = mockk()

        controller.onMediaUpgradeDeclined(offer, activity)
        verify { dialogController.dismissCurrentDialog() }
        verify(exactly = 0) { checkMediaUpgradePermissionsUseCase(offer, any()) }

        verify { activity.finish() }
        verify(exactly = 0) { acceptMediaUpgradeOfferUseCase(offer) }
        verify { declineMediaUpgradeOfferUseCase(offer) }

        confirmVerified(activity, offer)

    }

    @Test
    fun `screen-sharing state Requested will show EnableScreenSharingNotificationsAndStartSharingDialog when notification channel is not enabled`() {
        every { hasScreenSharingNotificationChannelEnabledUseCase() } returns false

        screenSharingProcessor.onNext(ScreenSharingState.Requested)

        verify { dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog() }
        verify(exactly = 0) { dialogController.showStartScreenSharingDialog() }
    }

    @Test
    fun `screen-sharing state Requested will show ScreenSharingDialog when notification channel is enabled`() {
        every { hasScreenSharingNotificationChannelEnabledUseCase() } returns true

        screenSharingProcessor.onNext(ScreenSharingState.Requested)

        verify(exactly = 0) { dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog() }
        verify { dialogController.showStartScreenSharingDialog() }
    }

    @Test
    fun `screen-sharing state RequestAccepted will show OverlayDialog when engagement is CV and overlay dialog needed`() {
        every { isCurrentEngagementCallVisualizerUseCase() } returns true
        every { isShowOverlayPermissionRequestDialogUseCase() } returns true

        screenSharingProcessor.onNext(ScreenSharingState.RequestAccepted)

        verify { isCurrentEngagementCallVisualizerUseCase() }
        verify { isShowOverlayPermissionRequestDialogUseCase() }
        verify { dialogController.showCVOverlayPermissionDialog() }

        confirmVerified(isCurrentEngagementCallVisualizerUseCase, isShowOverlayPermissionRequestDialogUseCase)
    }

    @Test
    fun `screen-sharing state Ended will remove notification`() {
        screenSharingProcessor.onNext(ScreenSharingState.Ended)

        verify { removeScreenSharingNotificationUseCase() }

        confirmVerified(removeScreenSharingNotificationUseCase, showScreenSharingNotificationUseCase)
    }

    @Test
    fun `screen-sharing state FailedToAcceptRequest will remove notification`() {
        val message = "_message"
        val state = controller.state.test()
        screenSharingProcessor.onNext(ScreenSharingState.FailedToAcceptRequest(message))

        verify { removeScreenSharingNotificationUseCase() }

        state.assertNotComplete().assertValue { it.value == OperatorRequestContract.State.DisplayToast(message) }

        confirmVerified(removeScreenSharingNotificationUseCase, showScreenSharingNotificationUseCase)
    }

    @Test
    fun `onShowEnableScreenSharingNotificationsAccepted will produce OpenNotificationsScreen state`() {
        val state = controller.state.test()

        controller.onShowEnableScreenSharingNotificationsAccepted()
        verify { dialogController.dismissCurrentDialog() }

        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestContract.State.OpenNotificationsScreen))
    }

    @Test
    fun `onShowEnableScreenSharingNotificationsDeclined will decline request and finish activity when it is DialogHolderActivity`() {
        val activity: DialogHolderActivity = mockk(relaxUnitFun = true)

        controller.onShowEnableScreenSharingNotificationsDeclined(activity)
        verify { dialogController.dismissCurrentDialog() }
        verify { screenSharingUseCase.declineRequest() }
        verify { activity.finish() }

        confirmVerified(activity)
    }

    @Test
    fun `onShowEnableScreenSharingNotificationsDeclined will decline request and not finish the activity when it is not DialogHolderActivity`() {
        val activity: ChatActivity = mockk(relaxUnitFun = true)

        controller.onShowEnableScreenSharingNotificationsDeclined(activity)
        verify { dialogController.dismissCurrentDialog() }
        verify { screenSharingUseCase.declineRequest() }
        verify(exactly = 0) { activity.finish() }

        confirmVerified(activity)
    }

    @Test
    fun `onScreenSharingDialogAccepted will accept request with acceptRequestWithAskedPermission`() {
        val activity: DialogHolderActivity = mockk(relaxUnitFun = true)

        val state = controller.state.test()

        controller.onScreenSharingDialogAccepted(activity)
        verify { dialogController.dismissCurrentDialog() }
        verify { showScreenSharingNotificationUseCase() }
        verify { gliaSdkConfigurationManager.screenSharingMode }
        verify { screenSharingUseCase.acceptRequestWithAskedPermission(eq(activity), any()) }

        state.assertNotComplete().assertValue { it.value == OperatorRequestContract.State.AcquireMediaProjectionToken }
        confirmVerified(activity, showScreenSharingNotificationUseCase, screenSharingUseCase, gliaSdkConfigurationManager)
    }

    @Test
    fun `onScreenSharingDialogDeclined will decline request and finish activity when it is DialogHolderActivity`() {
        val activity: DialogHolderActivity = mockk(relaxUnitFun = true)

        controller.onScreenSharingDialogDeclined(activity)
        verify { dialogController.dismissCurrentDialog() }
        verify { screenSharingUseCase.declineRequest() }
        verify { activity.finish() }

        confirmVerified(activity)
    }

    @Test
    fun `onScreenSharingDialogDeclined will decline request and not activity when it is not DialogHolderActivity`() {
        val activity: ChatActivity = mockk(relaxUnitFun = true)

        controller.onScreenSharingDialogDeclined(activity)
        verify { dialogController.dismissCurrentDialog() }
        verify { screenSharingUseCase.declineRequest() }
        verify(exactly = 0) { activity.finish() }

        confirmVerified(activity)
    }

    @Test
    fun `onReturnedFromNotificationScreen will show EnableScreenSharingNotificationsAndStartSharingDialog when notification channel is not enabled`() {
        every { hasScreenSharingNotificationChannelEnabledUseCase() } returns false

        controller.onReturnedFromNotificationScreen()

        verify { dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog() }
        verify(exactly = 0) { dialogController.showStartScreenSharingDialog() }
    }

    @Test
    fun `onReturnedFromNotificationScreen will show ScreenSharingDialog when notification channel is enabled`() {
        every { hasScreenSharingNotificationChannelEnabledUseCase() } returns true

        controller.onReturnedFromNotificationScreen()

        verify(exactly = 0) { dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog() }
        verify { dialogController.showStartScreenSharingDialog() }
    }

    @Test
    fun `onNotificationScreenRequested will produce WaitForNotificationScreenOpen state`() {
        val state = controller.state.test()

        controller.onNotificationScreenRequested()

        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestContract.State.WaitForNotificationScreenOpen))
    }

    @Test
    fun `onNotificationScreenOpened will produce WaitForNotificationScreenResult state`() {
        val state = controller.state.test()

        controller.onNotificationScreenOpened()

        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestContract.State.WaitForNotificationScreenResult))
    }

    @Test
    fun `failedToOpenOverlayPermissionScreen will log message`() {
        mockkStatic(LOGGER_PATH)
        Logger.setIsDebug(false)
        every { Logger.d(any(), any()) } just Runs

        controller.failedToOpenOverlayPermissionScreen()
        verify { Logger.d(any(), any()) }

        unmockkStatic(LOGGER_PATH)
    }

    @Test
    fun `overlayPermissionScreenOpened will log message`() {
        mockkStatic(LOGGER_PATH)
        Logger.setIsDebug(false)
        every { Logger.d(any(), any()) } just Runs

        controller.overlayPermissionScreenOpened()
        verify { Logger.d(any(), any()) }

        unmockkStatic(LOGGER_PATH)
    }

    @Test
    fun `onOverlayPermissionRequestDeclined will set overlay dialog shown`() {
        mockkStatic(LOGGER_PATH)
        Logger.setIsDebug(false)
        every { Logger.d(any(), any()) } just Runs

        controller.onOverlayPermissionRequestDeclined()
        verify { dialogController.dismissCurrentDialog() }
        verify { setOverlayPermissionRequestDialogShownUseCase() }
        verify { Logger.d(any(), any()) }

        confirmVerified(dialogController, setOverlayPermissionRequestDialogShownUseCase)
        unmockkStatic(LOGGER_PATH)
    }

    @Test
    fun `onOverlayPermissionRequestAccepted will open overlay permission screen`() {
        mockkStatic(LOGGER_PATH)
        Logger.setIsDebug(false)
        every { Logger.d(any(), any()) } just Runs

        val state = controller.state.test()

        controller.onOverlayPermissionRequestAccepted()
        verify { dialogController.dismissCurrentDialog() }
        verify { setOverlayPermissionRequestDialogShownUseCase() }
        verify { Logger.d(any(), any()) }

        state.assertNotComplete().assertValue { it.value == OperatorRequestContract.State.OpenOverlayPermissionScreen }

        confirmVerified(dialogController, setOverlayPermissionRequestDialogShownUseCase)
        unmockkStatic(LOGGER_PATH)
    }

    @Test
    fun `onMediaProjectionResultReceived will skip permission request when result is successful`() {
        val activity: DialogHolderActivity = mockk(relaxed = true)
        val intent: Intent = mockk(relaxUnitFun = true)
        val result: ActivityResult = mockk(relaxUnitFun = true) {
            every { resultCode } returns Activity.RESULT_OK
            every { data } returns intent
        }

        controller.onMediaProjectionResultReceived(result, activity)

        verify { result.resultCode }
        verify { result.data }

        verify { activity.finish() }

        verify { screenSharingUseCase.onActivityResultSkipPermissionRequest(Activity.RESULT_OK, intent) }

        confirmVerified(activity, intent, result, screenSharingUseCase)
    }

    @Test
    fun `onMediaProjectionResultReceived will decline request when result is not successful`() {
        val activity: ChatActivity = mockk(relaxed = true)
        val result: ActivityResult = mockk(relaxUnitFun = true) {
            every { resultCode } returns Activity.RESULT_OK
            every { data } returns null
        }

        controller.onMediaProjectionResultReceived(result, activity)

        verify { result.resultCode }
        verify { result.data }

        verify(exactly = 0) { activity.finish() }

        verify { screenSharingUseCase.declineRequest() }
        verify { removeScreenSharingNotificationUseCase() }

        confirmVerified(activity, result, screenSharingUseCase, removeScreenSharingNotificationUseCase)
    }

}
