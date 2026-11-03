package com.glia.widgets.internal.chathead

import android.GLIA_LOGGER_PATH
import android.mock
import android.unMock
import com.glia.androidsdk.Operator
import com.glia.widgets.callbacks.OnResult
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.EndAction
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.internal.chathead.domain.DecideBubbleRenderTargetUseCase
import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase
import com.glia.widgets.secureconversations.SecureConversations
import com.glia.widgets.view.MessagesNotSeenHandler
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.processors.PublishProcessor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Optional

internal class ChatBubbleControllerTest {

    private val engagementState: PublishProcessor<State> = PublishProcessor.create()
    private val currentOperator: PublishProcessor<Operator> = PublishProcessor.create()
    private val onHoldState: PublishProcessor<Boolean> = PublishProcessor.create()

    private lateinit var decideBubbleRenderTargetUseCase: DecideBubbleRenderTargetUseCase
    private lateinit var resolveChatHeadNavigationUseCase: ResolveChatHeadNavigationUseCase
    private lateinit var isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase
    private lateinit var isFromCallScreenUseCase: IsFromCallScreenUseCase
    private lateinit var updateFromCallScreenUseCase: UpdateFromCallScreenUseCase
    private lateinit var chatHeadManager: ChatHeadManager
    private lateinit var messagesNotSeenHandler: MessagesNotSeenHandler
    private lateinit var secureConversations: SecureConversations

    private lateinit var decidedContexts: MutableList<BubbleContext>
    private lateinit var unreadListener: CapturingSlot<MessagesNotSeenHandler.MessagesNotSeenHandlerListener>
    private lateinit var retainCallback: CapturingSlot<OnResult<Int>>

    private var target: BubbleRenderTarget = BubbleRenderTarget.NONE
    private var isServiceNeeded: Boolean = false

    private lateinit var controller: ChatBubbleController

    @Before
    fun setUp() {
        Logger.mock()
        mockkStatic(GLIA_LOGGER_PATH)
        Dependencies.mock()

        secureConversations = mockk()
        retainCallback = slot()
        every { secureConversations.subscribeToUnreadMessageCount(capture(retainCallback)) } just runs
        every { secureConversations.unSubscribeFromUnreadMessageCount(any()) } just runs
        every { Dependencies.secureConversations } returns secureConversations

        decidedContexts = mutableListOf()
        decideBubbleRenderTargetUseCase = mockk()
        every { decideBubbleRenderTargetUseCase(capture(decidedContexts)) } answers { target }
        every { decideBubbleRenderTargetUseCase.isOverlayServiceNeeded } answers { isServiceNeeded }

        resolveChatHeadNavigationUseCase = mockk()
        isCurrentEngagementCallVisualizerUseCase = mockk()
        every { isCurrentEngagementCallVisualizerUseCase() } returns false
        isFromCallScreenUseCase = mockk()
        updateFromCallScreenUseCase = mockk(relaxUnitFun = true)
        chatHeadManager = mockk(relaxUnitFun = true)

        unreadListener = slot()
        messagesNotSeenHandler = mockk()
        every { messagesNotSeenHandler.addListener(capture(unreadListener)) } just runs

        val engagementStateUseCase: EngagementStateUseCase = mockk()
        every { engagementStateUseCase() } returns engagementState
        val currentOperatorUseCase: CurrentOperatorUseCase = mockk()
        every { currentOperatorUseCase() } returns currentOperator
        val visitorMediaUseCase: VisitorMediaUseCase = mockk()
        every { visitorMediaUseCase.onHoldState } returns onHoldState

        controller = ChatBubbleController(
            decideBubbleRenderTargetUseCase = decideBubbleRenderTargetUseCase,
            resolveChatHeadNavigationUseCase = resolveChatHeadNavigationUseCase,
            isCurrentEngagementCallVisualizerUseCase = isCurrentEngagementCallVisualizerUseCase,
            isFromCallScreenUseCase = isFromCallScreenUseCase,
            updateFromCallScreenUseCase = updateFromCallScreenUseCase,
            chatHeadManager = chatHeadManager,
            messagesNotSeenHandler = messagesNotSeenHandler,
            engagementStateUseCase = engagementStateUseCase,
            currentOperatorUseCase = currentOperatorUseCase,
            visitorMediaUseCase = visitorMediaUseCase
        )
    }

    @After
    fun tearDown() {
        Logger.unMock()
        unmockkStatic(GLIA_LOGGER_PATH)
        Dependencies.unMock()
    }

    private fun operatorMock(imageUrl: String?): Operator = mockk {
        every { id } returns "operator-id"
        every { picture } returns mockk { every { url } returns Optional.ofNullable(imageUrl) }
    }

    // region Engagement phases

    @Test
    fun `initial state renders nothing`() {
        assertEquals(BubbleUiModel.INITIAL, controller.uiState.value)
    }

    @Test
    fun `queueing shows the queueing content`() {
        // When
        engagementState.onNext(State.Queuing("queueId", MediaType.TEXT))

        // Then
        assertEquals(BubbleContent.Queueing, controller.uiState.value.content)
    }

    @Test
    fun `pre queueing shows the queueing content`() {
        // When
        engagementState.onNext(State.PreQueuing(MediaType.TEXT))

        // Then
        assertEquals(BubbleContent.Queueing, controller.uiState.value.content)
    }

    @Test
    fun `engagement without a loaded operator shows the placeholder`() {
        // When
        engagementState.onNext(State.EngagementStarted(false))

        // Then
        assertEquals(BubbleContent.Engaged(null), controller.uiState.value.content)
    }

    @Test
    fun `engagement with an operator image shows that image`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))

        // When
        currentOperator.onNext(operatorMock("https://operator.image"))

        // Then
        assertEquals(BubbleContent.Engaged("https://operator.image"), controller.uiState.value.content)
    }

    @Test
    fun `engagement with an operator without a picture shows the placeholder`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))

        // When
        currentOperator.onNext(operatorMock(null))

        // Then
        assertEquals(BubbleContent.Engaged(null), controller.uiState.value.content)
    }

    @Test
    fun `engagement update keeps the engaged content`() {
        // When
        engagementState.onNext(mockk<State.Update>())

        // Then
        assertEquals(BubbleContent.Engaged(null), controller.uiState.value.content)
    }

    @Test
    fun `engagement end resets the bubble state`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))
        currentOperator.onNext(operatorMock("https://operator.image"))
        unreadListener.captured.onNewCount(4)

        // When
        engagementState.onNext(State.EngagementEnded(EndAction.ShowEndDialog))

        // Then
        assertEquals(
            BubbleUiModel(BubbleRenderTarget.NONE, BubbleContent.Ended, 0, false, false),
            controller.uiState.value
        )
    }

    @Test
    fun `queue unstaffed resets the bubble state`() {
        // Given
        engagementState.onNext(State.Queuing("queueId", MediaType.TEXT))

        // When
        engagementState.onNext(State.QueueUnstaffed)

        // Then
        assertEquals(BubbleContent.Ended, controller.uiState.value.content)
    }

    @Test
    fun `unexpected error resets the bubble state`() {
        // Given
        engagementState.onNext(State.Queuing("queueId", MediaType.TEXT))

        // When
        engagementState.onNext(State.UnexpectedErrorHappened)

        // Then
        assertEquals(BubbleContent.Ended, controller.uiState.value.content)
    }

    @Test
    fun `queueing cancelled resets the bubble state`() {
        // Given
        engagementState.onNext(State.Queuing("queueId", MediaType.TEXT))

        // When
        engagementState.onNext(State.QueueingCanceled)

        // Then
        assertEquals(BubbleContent.Ended, controller.uiState.value.content)
    }

    // endregion

    // region Retain

    @Test
    fun `engagement end with Retain keeps the bubble while there are unread messages`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))
        unreadListener.captured.onNewCount(2)

        // When
        engagementState.onNext(State.EngagementEnded(EndAction.Retain))

        // Then
        verify { secureConversations.subscribeToUnreadMessageCount(any()) }
        assertEquals(BubbleContent.Engaged(null), controller.uiState.value.content)
        assertEquals(2, controller.uiState.value.unreadCount)
    }

    @Test
    fun `retained bubble is reset once the unread count reaches zero`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))
        unreadListener.captured.onNewCount(2)
        engagementState.onNext(State.EngagementEnded(EndAction.Retain))

        // When
        retainCallback.captured.onResult(0)

        // Then
        assertEquals(BubbleContent.Ended, controller.uiState.value.content)
        assertEquals(0, controller.uiState.value.unreadCount)
        verify { secureConversations.unSubscribeFromUnreadMessageCount(any()) }
    }

    @Test
    fun `retained bubble stays while the unread count is above zero`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))
        engagementState.onNext(State.EngagementEnded(EndAction.Retain))

        // When
        retainCallback.captured.onResult(3)

        // Then
        assertEquals(BubbleContent.Engaged(null), controller.uiState.value.content)
        verify(exactly = 0) { secureConversations.unSubscribeFromUnreadMessageCount(any()) }
    }

    // endregion

    // region Unread badge

    @Test
    fun `unread count is published`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))

        // When
        unreadListener.captured.onNewCount(7)

        // Then
        assertEquals(7, controller.uiState.value.unreadCount)
    }

    @Test
    fun `unread count is suppressed during a Call Visualizer engagement`() {
        // Given
        every { isCurrentEngagementCallVisualizerUseCase() } returns true
        engagementState.onNext(State.EngagementStarted(true))

        // When
        unreadListener.captured.onNewCount(7)

        // Then
        assertEquals(0, controller.uiState.value.unreadCount)
        assertEquals(BubbleContent.Engaged(null), controller.uiState.value.content)
    }

    // endregion

    // region On hold

    @Test
    fun `on hold is published during an engagement`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))

        // When
        onHoldState.onNext(true)

        // Then
        assertTrue(controller.uiState.value.isOnHold)
    }

    @Test
    fun `on hold is ignored while queueing`() {
        // Given
        engagementState.onNext(State.Queuing("queueId", MediaType.TEXT))

        // When
        onHoldState.onNext(true)

        // Then
        assertFalse(controller.uiState.value.isOnHold)
    }

    @Test
    fun `on hold is cleared when a new engagement starts`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))
        onHoldState.onNext(true)

        // When
        engagementState.onNext(State.EngagementStarted(false))

        // Then
        assertFalse(controller.uiState.value.isOnHold)
    }

    @Test
    fun `on hold is cleared when the engagement ends`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))
        onHoldState.onNext(true)

        // When
        engagementState.onNext(State.EngagementEnded(EndAction.ShowEndDialog))

        // Then
        assertFalse(controller.uiState.value.isOnHold)
    }

    // endregion

    // region Overlay service lifetime

    @Test
    fun `overlay service starts while the app is still foregrounded`() {
        // Given the in-app bubble is the visible layer, but the overlay could be needed later
        target = BubbleRenderTarget.APPLICATION
        isServiceNeeded = true

        // When
        engagementState.onNext(State.EngagementStarted(false))

        // Then the service is up before the app can be backgrounded
        assertEquals(BubbleRenderTarget.APPLICATION, controller.uiState.value.target)
        verify { chatHeadManager.startChatHeadService() }
        verify(exactly = 0) { chatHeadManager.stopChatHeadService() }
    }

    @Test
    fun `overlay service keeps running across the move into the background`() {
        // Given
        target = BubbleRenderTarget.APPLICATION
        isServiceNeeded = true
        engagementState.onNext(State.EngagementStarted(false))

        // When
        target = BubbleRenderTarget.SERVICE
        controller.onContextEvent(BubbleContextEvent.AppBackgrounded)

        // Then it was never stopped in between - only the window the service owns changes
        assertEquals(BubbleRenderTarget.SERVICE, controller.uiState.value.target)
        verify(exactly = 0) { chatHeadManager.stopChatHeadService() }
    }

    @Test
    fun `overlay service is not started when it is not needed`() {
        // Given the integrator disabled it, or the overlay permission is missing
        target = BubbleRenderTarget.APPLICATION
        isServiceNeeded = false

        // When
        engagementState.onNext(State.EngagementStarted(false))

        // Then
        verify(exactly = 0) { chatHeadManager.startChatHeadService() }
        verify { chatHeadManager.stopChatHeadService() }
    }

    @Test
    fun `a start refused by the OS is retried on the next state change`() {
        // Given ChatHeadManager swallows the background-start IllegalStateException, so the controller
        // asks again rather than tracking the edge itself
        isServiceNeeded = true
        engagementState.onNext(State.EngagementStarted(false))

        // When anything else changes
        onHoldState.onNext(true)
        unreadListener.captured.onNewCount(1)

        // Then
        verify(atLeast = 3) { chatHeadManager.startChatHeadService() }
    }

    @Test
    fun `engagement end stops the overlay service`() {
        // Given
        target = BubbleRenderTarget.SERVICE
        isServiceNeeded = true
        engagementState.onNext(State.EngagementStarted(false))

        // When the engagement is over no bubble is needed any more
        target = BubbleRenderTarget.NONE
        isServiceNeeded = false
        engagementState.onNext(State.EngagementEnded(EndAction.ShowEndDialog))

        // Then
        verify { chatHeadManager.stopChatHeadService() }
    }

    // endregion

    // region Context derivation

    @Test
    fun `a resumed screen is reported as the in app context`() {
        // When
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.OTHER))

        // Then
        assertEquals(BubbleContext.InApp(VisibleScreen.OTHER), decidedContexts.last())
    }

    @Test
    fun `the reported screen replaces the previous one`() {
        // Given the host resolves which activity is on top, so the controller just stores it
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.CALL))

        // When
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.CHAT))

        // Then
        assertEquals(BubbleContext.InApp(VisibleScreen.CHAT), decidedContexts.last())
    }

    @Test
    fun `the chat screen is marked on the model for the view to pick its theme`() {
        // When
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.CHAT))

        // Then
        assertTrue(controller.uiState.value.isOnChatScreen)
    }

    @Test
    fun `any other screen is not marked as the chat screen`() {
        // Given
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.CHAT))

        // When
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.OTHER))

        // Then
        assertFalse(controller.uiState.value.isOnChatScreen)
    }

    @Test
    fun `a backgrounded app is not on the chat screen`() {
        // Given
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.CHAT))

        // When
        controller.onContextEvent(BubbleContextEvent.AppBackgrounded)

        // Then
        assertFalse(controller.uiState.value.isOnChatScreen)
    }

    @Test
    fun `no reported screen is the background context`() {
        // Given
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.OTHER))

        // When nothing is resumed any more
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(null))

        // Then
        assertEquals(BubbleContext.AppInBackground, decidedContexts.last())
    }

    @Test
    fun `every reported screen re-evaluates the target`() {
        // Given
        val decisionsBefore = decidedContexts.size

        // When
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.OTHER))

        // Then
        assertEquals(decisionsBefore + 1, decidedContexts.size)
    }

    @Test
    fun `backgrounding the app reports the background context`() {
        // Given
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.OTHER))

        // When
        controller.onContextEvent(BubbleContextEvent.AppBackgrounded)

        // Then
        assertEquals(BubbleContext.AppInBackground, decidedContexts.last())
    }

    @Test
    fun `a screen resumed after backgrounding is tracked again`() {
        // Given
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.OTHER))
        controller.onContextEvent(BubbleContextEvent.AppBackgrounded)

        // When
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(VisibleScreen.MESSAGE_CENTER))

        // Then
        assertEquals(BubbleContext.InApp(VisibleScreen.MESSAGE_CENTER), decidedContexts.last())
    }

    // endregion

    // region Tap and teardown

    @Test
    fun `tapping the bubble returns the call destination for a media engagement`() {
        // Given
        every { resolveChatHeadNavigationUseCase.execute() } returns ResolveChatHeadNavigationUseCase.Destinations.CALL_VIEW

        // When
        val destination = controller.onBubbleTapped()

        // Then
        assertEquals(ResolveChatHeadNavigationUseCase.Destinations.CALL_VIEW, destination)
    }

    @Test
    fun `tapping the bubble returns the chat destination otherwise`() {
        // Given
        every { resolveChatHeadNavigationUseCase.execute() } returns ResolveChatHeadNavigationUseCase.Destinations.CHAT_VIEW

        // When
        val destination = controller.onBubbleTapped()

        // Then
        assertEquals(ResolveChatHeadNavigationUseCase.Destinations.CHAT_VIEW, destination)
    }

    @Test
    fun `isFromCallScreen delegates to the use case`() {
        // Given
        every { isFromCallScreenUseCase() } returns true

        // Then
        assertTrue(controller.isFromCallScreen())
    }

    @Test
    fun `resetFromCallScreen delegates to the use case`() {
        // When
        controller.resetFromCallScreen()

        // Then
        verify { updateFromCallScreenUseCase(false) }
    }

    @Test
    fun `onDestroy stops the overlay service and drops the unread subscription`() {
        // Given
        target = BubbleRenderTarget.SERVICE
        engagementState.onNext(State.EngagementStarted(false))
        target = BubbleRenderTarget.NONE

        // When
        controller.onDestroy()

        // Then
        assertEquals(BubbleUiModel(BubbleRenderTarget.NONE, BubbleContent.Ended, 0, false, false), controller.uiState.value)
        verify { chatHeadManager.stopChatHeadService() }
        verify { secureConversations.unSubscribeFromUnreadMessageCount(any()) }
    }

    @Test
    fun `bubble position survives being written and read back`() {
        // Given
        val position = BubblePosition(0.25f, 0.75f)

        // When
        controller.bubblePosition = position

        // Then
        assertEquals(position, controller.bubblePosition)
    }

    // endregion
}
