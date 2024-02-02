package com.glia.widgets.operator

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.CheckMediaUpgradePermissionsUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.engagement.domain.OperatorMediaUpgradeOfferUseCase
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.isAudio
import com.glia.widgets.view.dialog.holder.DialogHolderActivity
import io.mockk.CapturingSlot
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.processors.PublishProcessor
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val COMMON_EXTENSIONS_CLASS_PATH = "com.glia.widgets.helper.CommonExtensionsKt"

class OperatorRequestHandlerControllerTest {
    private val mediaUpgradeRequest = PublishProcessor.create<MediaUpgradeOfferData>()
    private val acceptMediaUpgradeRequestResult = PublishProcessor.create<MediaUpgradeOffer>()

    private lateinit var operatorMediaUpgradeOfferUseCase: OperatorMediaUpgradeOfferUseCase
    private lateinit var acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase
    private lateinit var declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase
    private lateinit var checkMediaUpgradePermissionsUseCase: CheckMediaUpgradePermissionsUseCase
    private lateinit var dialogController: DialogContract.Controller
    private lateinit var dialogCallbackSlot: CapturingSlot<DialogContract.Controller.Callback>

    private lateinit var controller: OperatorRequestHandlerContract.Controller

    @Before
    fun setUp() {
        operatorMediaUpgradeOfferUseCase = mockk(relaxUnitFun = true)
        every { operatorMediaUpgradeOfferUseCase() } returns mediaUpgradeRequest

        acceptMediaUpgradeOfferUseCase = mockk(relaxUnitFun = true) {
            every { result } returns acceptMediaUpgradeRequestResult
        }
        declineMediaUpgradeOfferUseCase = mockk(relaxUnitFun = true)
        checkMediaUpgradePermissionsUseCase = mockk(relaxUnitFun = true)
        dialogController = mockk(relaxUnitFun = true)

        dialogCallbackSlot = slot()

        controller = OperatorRequestHandlerController(
            operatorMediaUpgradeOfferUseCase,
            acceptMediaUpgradeOfferUseCase,
            declineMediaUpgradeOfferUseCase,
            checkMediaUpgradePermissionsUseCase,
            dialogController
        )

        verify { operatorMediaUpgradeOfferUseCase() }
        verify { acceptMediaUpgradeOfferUseCase.result }
        verify { dialogController.addCallback(capture(dialogCallbackSlot)) }
    }

    @After
    fun tearDown() {
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
        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestHandlerContract.State.OpenCallActivity(Engagement.MediaType.AUDIO)))

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
        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestHandlerContract.State.OpenCallActivity(Engagement.MediaType.VIDEO)))

        unmockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `handleDialogCallback will produce RequestMediaUpgrade state when MediaUpgrade dialog state is triggered`() {
        val data: MediaUpgradeOfferData = mockk()
        val state = controller.state.test()

        dialogCallbackSlot.captured.emitDialogState(DialogState.MediaUpgrade(data))

        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestHandlerContract.State.RequestMediaUpgrade(data)))
    }

    @Test
    fun `handleDialogCallback will produce DismissAlertDialog state when None dialog state is triggered`() {
        val state = controller.state.test()

        dialogCallbackSlot.captured.emitDialogState(DialogState.None)

        state.assertNotComplete().assertValue(OneTimeEvent(OperatorRequestHandlerContract.State.DismissAlertDialog))
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

}
