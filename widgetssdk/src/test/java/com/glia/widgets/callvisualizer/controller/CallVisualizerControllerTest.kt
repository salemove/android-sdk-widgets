package com.glia.widgets.callvisualizer.controller

import com.glia.androidsdk.IncomingEngagementRequest
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementRequestUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.OnIncomingEngagementRequestTimeoutUseCase
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.webbrowser.domain.GetUrlFromLinkUseCase
import io.mockk.CapturingSlot
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class CallVisualizerControllerTest {
    private val incomingEngagementProcessor: PublishProcessor<IncomingEngagementRequest> = PublishProcessor.create()
    private val incomingEngagementRequestTimeoutProcessor: PublishProcessor<Unit> = PublishProcessor.create()
    private val engagementStateProcessor: PublishProcessor<State> = PublishProcessor.create()

    private lateinit var dialogController: DialogContract.Controller
    private lateinit var confirmationDialogUseCase: ConfirmationDialogUseCase
    private lateinit var engagementRequestUseCase: EngagementRequestUseCase
    private lateinit var engagementStateUseCase: EngagementStateUseCase
    private lateinit var confirmationDialogLinksUseCase: ConfirmationDialogLinksUseCase
    private lateinit var getUrlFromLinkUseCase: GetUrlFromLinkUseCase
    private lateinit var onIncomingEngagementRequestTimeoutUseCase: OnIncomingEngagementRequestTimeoutUseCase

    private lateinit var dialogCallback: CapturingSlot<DialogContract.Controller.Callback>

    private lateinit var controller: CallVisualizerContract.Controller

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        dialogController = mockk(relaxUnitFun = true)
        confirmationDialogUseCase = mockk(relaxUnitFun = true)

        engagementRequestUseCase = mockk(relaxUnitFun = true)
        every { engagementRequestUseCase() } returns incomingEngagementProcessor

        engagementStateUseCase = mockk()
        every { engagementStateUseCase() } returns engagementStateProcessor

        confirmationDialogLinksUseCase = mockk(relaxed = true)

        getUrlFromLinkUseCase = mockk()

        dialogCallback = slot()

        onIncomingEngagementRequestTimeoutUseCase = mockk()
        every { onIncomingEngagementRequestTimeoutUseCase() } returns incomingEngagementRequestTimeoutProcessor

        controller = CallVisualizerController(
            dialogController,
            confirmationDialogUseCase,
            engagementRequestUseCase,
            engagementStateUseCase,
            confirmationDialogLinksUseCase,
            getUrlFromLinkUseCase,
            onIncomingEngagementRequestTimeoutUseCase
        )

        verify { dialogController.addCallback(capture(dialogCallback)) }
        verify { engagementRequestUseCase() }
        verify { engagementStateUseCase() }
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()

        confirmVerified(dialogController, confirmationDialogUseCase, engagementRequestUseCase, engagementStateUseCase, confirmationDialogLinksUseCase)
    }

    @Test
    fun `CV engagement start will trigger will dismiss visitor code dialog`() {
        engagementStateProcessor.onNext(State.StartedCallVisualizer)
        verify { dialogController.dismissVisitorCodeDialog() }
    }

    @Test
    fun `CV engagement end will emit engagementEndFlow`() {
        val engagementEnd = controller.engagementEndFlow.test()

        verify { engagementStateUseCase() }

        engagementStateProcessor.onNext(State.FinishedCallVisualizer)

        engagementEnd.assertNotComplete().assertValueCount(1)
    }

    @Test
    fun `test dialog state mapper`() {
        val testState = controller.state.map { it.value }.test()

        emitDialogState(DialogState.VisitorCode)
        emitDialogState(DialogState.None)
        emitDialogState(DialogState.CVConfirmation)

        testState.assertNotComplete().assertValueCount(3).values().run {
            assertTrue(get(0) is CallVisualizerContract.State.DisplayVisitorCodeDialog)
            assertTrue(get(1) is CallVisualizerContract.State.DismissDialog)
            assertTrue(get(2) is CallVisualizerContract.State.DisplayConfirmationDialog)
        }

        verify { confirmationDialogLinksUseCase() }
    }

    @Test
    fun `showVisitorCodeDialog shows visitor code dialog`() {
        controller.showVisitorCodeDialog()
        verify { dialogController.showVisitorCodeDialog() }
    }

    @Test
    fun `engagement request should show visitor confirmation dialog when it is necessary`() {
        val confirmationSlot = slot<(shouldShow: Boolean) -> Unit>()
        incomingEngagementProcessor.onNext(mockk())
        verify { dialogController.dismissVisitorCodeDialog() }
        verify { confirmationDialogUseCase(capture(confirmationSlot)) }

        confirmationSlot.captured.invoke(true)
        verify { dialogController.showCVEngagementConfirmationDialog() }
        verify(exactly = 0) { engagementRequestUseCase.accept(any()) }
    }

    @Test
    fun `engagement request should accept engagement request when confirmation is not necessary`() {
        val assetId = "asset_id"
        controller.saveVisitorContextAssetId(assetId)

        val confirmationSlot = slot<(shouldShow: Boolean) -> Unit>()
        incomingEngagementProcessor.onNext(mockk())
        verify { dialogController.dismissVisitorCodeDialog() }
        verify { confirmationDialogUseCase(capture(confirmationSlot)) }

        confirmationSlot.captured.invoke(false)
        verify(exactly = 0) { dialogController.showCVEngagementConfirmationDialog() }
        verify { engagementRequestUseCase.accept(eq(assetId)) }
    }

    @Test
    fun `onLinkClicked will produce OpenWebBrowserScreen state`() {
        val urlStringKey = 123
        val url = "https://glia.com"
        val urlLocaleString = LocaleString(urlStringKey, emptyList())
        val link = Link(mockk<LocaleString>(), urlLocaleString)

        every { getUrlFromLinkUseCase(link) } returns url

        val testState = controller.state.test()

        controller.onLinkClicked(link)

        verify { dialogController.dismissCurrentDialog() }
        verify { getUrlFromLinkUseCase(link) }

        testState.assertNotComplete().assertValue {
            it.value == CallVisualizerContract.State.OpenWebBrowserScreen(link.title, url)
        }
    }

    @Test
    fun `onEngagementConfirmationDialogAllowed will accept engagement request`() {
        val testState = controller.state.test()

        controller.onEngagementConfirmationDialogAllowed()

        testState.assertNotComplete().assertValue { it.value is CallVisualizerContract.State.CloseHolderActivity }
        verify { engagementRequestUseCase.accept(any()) }
        verify { dialogController.dismissCurrentDialog() }
    }

    @Test
    fun `onEngagementConfirmationDialogDeclined will decline engagement request`() {
        val testState = controller.state.test()

        controller.onEngagementConfirmationDialogDeclined()

        testState.assertNotComplete().assertValue { it.value is CallVisualizerContract.State.CloseHolderActivity }
        verify { engagementRequestUseCase.decline() }
        verify { dialogController.dismissCurrentDialog() }
    }

    @Test
    fun `dismissVisitorCodeDialog will dismiss visitor code dialog`() {
        val testState = controller.state.test()

        controller.dismissVisitorCodeDialog()

        testState.assertNotComplete().assertValue { it.value is CallVisualizerContract.State.CloseHolderActivity }
        verify { dialogController.dismissVisitorCodeDialog() }
    }

    @Test
    fun `showAlreadyInCallSnackBar will show snackBar`() {
        val testState = controller.state.test()

        controller.showAlreadyInCvSnackBar()

        testState.assertNotComplete().assertValue { it.value is CallVisualizerContract.State.ShowAlreadyInCvSnackBar }
    }

    @Test
    fun `onWebBrowserOpened will request to show CV Confirmation Dialog`() {
        controller.onWebBrowserOpened()

        verify { dialogController.showCVEngagementConfirmationDialog() }
    }

    @Test
    fun `incoming engagement request timeout will request to dismiss CV dialog`() {
        val testState = controller.state.map { it.value }.test()
        verify(exactly = 0) { dialogController.dismissCVEngagementConfirmationDialog() }

        incomingEngagementRequestTimeoutProcessor.onNext(Unit)

        verify { dialogController.dismissCVEngagementConfirmationDialog() }

        testState.assertNotComplete().assertValueCount(2).values().run {
            assertTrue(get(0) is CallVisualizerContract.State.CloseHolderActivity)
            assertTrue(get(1) is CallVisualizerContract.State.ShowTimeoutSnackBar)
        }
    }

    private fun emitDialogState(state: DialogState) {
        dialogCallback.captured.emitDialogState(state)
    }
}
