package com.glia.widgets.operator

import android.COMMON_EXTENSIONS_CLASS_PATH
import android.LOGGER_PATH
import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.internal.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import com.glia.widgets.internal.dialog.model.DialogState
import com.glia.widgets.internal.permissions.domain.WithNotificationPermissionUseCase
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.CheckMediaUpgradePermissionsUseCase
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.engagement.domain.OperatorMediaUpgradeOfferUseCase
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.isAudio
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
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class OperatorRequestControllerTest {
    private val mediaUpgradeRequest = PublishProcessor.create<MediaUpgradeOfferData>()
    private val acceptMediaUpgradeRequestResult = PublishProcessor.create<MediaUpgradeOffer>()

    private lateinit var operatorMediaUpgradeOfferUseCase: OperatorMediaUpgradeOfferUseCase
    private lateinit var acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase
    private lateinit var declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase
    private lateinit var checkMediaUpgradePermissionsUseCase: CheckMediaUpgradePermissionsUseCase
    private lateinit var currentOperatorUseCase: CurrentOperatorUseCase
    private lateinit var isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase
    private lateinit var isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase
    private lateinit var setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase
    private lateinit var dialogController: DialogContract.Controller
    private lateinit var dialogCallbackSlot: CapturingSlot<DialogContract.Controller.Callback>
    private lateinit var withNotificationPermissionUseCase: WithNotificationPermissionUseCase

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
        currentOperatorUseCase = mockk(relaxUnitFun = true)
        isShowOverlayPermissionRequestDialogUseCase = mockk(relaxUnitFun = true)
        isCurrentEngagementCallVisualizerUseCase = mockk(relaxUnitFun = true)
        setOverlayPermissionRequestDialogShownUseCase = mockk(relaxUnitFun = true)
        withNotificationPermissionUseCase = mockk(relaxUnitFun = true)
        dialogController = mockk(relaxUnitFun = true)

        dialogCallbackSlot = slot()

        controller = OperatorRequestController(
            operatorMediaUpgradeOfferUseCase,
            acceptMediaUpgradeOfferUseCase,
            declineMediaUpgradeOfferUseCase,
            checkMediaUpgradePermissionsUseCase,
            currentOperatorUseCase,
            isShowOverlayPermissionRequestDialogUseCase,
            isCurrentEngagementCallVisualizerUseCase,
            setOverlayPermissionRequestDialogShownUseCase,
            dialogController,
            withNotificationPermissionUseCase
        )

        verify { operatorMediaUpgradeOfferUseCase() }
        verify { acceptMediaUpgradeOfferUseCase.result }
        verify { dialogController.addCallback(capture(dialogCallbackSlot)) }
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

        val activity: Activity = mockk(relaxUnitFun = true)
        controller.onOverlayPermissionRequestDeclined(activity)
        verify { dialogController.dismissCurrentDialog() }
        verify { setOverlayPermissionRequestDialogShownUseCase() }
        verify { Logger.d(any(), any()) }
        verify(exactly = 0) { activity.finish() }

        confirmVerified(dialogController, setOverlayPermissionRequestDialogShownUseCase, activity)
        unmockkStatic(LOGGER_PATH)
    }

    @Test
    fun `onOverlayPermissionRequestAccepted will open overlay permission screen`() {
        mockkStatic(LOGGER_PATH)
        Logger.setIsDebug(false)
        every { Logger.d(any(), any()) } just Runs

        val state = controller.state.test()

        val activity: DialogHolderActivity = mockk(relaxUnitFun = true)
        controller.onOverlayPermissionRequestAccepted(activity)
        verify { dialogController.dismissCurrentDialog() }
        verify { setOverlayPermissionRequestDialogShownUseCase() }
        verify { Logger.d(any(), any()) }
        verify { activity.finish() }

        state.assertNotComplete().assertValue { it.value == OperatorRequestContract.State.OpenOverlayPermissionScreen }

        confirmVerified(dialogController, setOverlayPermissionRequestDialogShownUseCase, activity)
        unmockkStatic(LOGGER_PATH)
    }
}
