package com.glia.widgets.internal.chathead

import android.GLIA_LOGGER_PATH
import android.app.Activity
import android.mock
import android.os.Looper
import android.unMock
import android.view.ViewGroup
import com.glia.androidsdk.Operator
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.GliaCoreImpl
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.hasChildOfType
import com.glia.widgets.helper.rootView
import com.glia.widgets.R
import com.glia.widgets.internal.chathead.domain.DecideBubbleRenderTargetUseCase
import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.secureconversations.SecureConversations
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.head.ActivityWatcherForChatHead
import com.glia.widgets.view.head.ChatHeadLayout
import com.glia.widgets.view.head.ChatHeadView
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.processors.PublishProcessor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import io.reactivex.rxjava3.core.Observable as RxObservable

/**
 * Drives the real [ChatBubbleController] through [ActivityWatcherForChatHead], because the bubble
 * rules and the host's attach/detach only combine into visible behaviour when both are real: the
 * controller publishes a conflating [kotlinx.coroutines.flow.StateFlow], so a host that only reacts
 * to *changes* silently loses the bubble whenever a lifecycle event leaves the model untouched.
 *
 * Configuration under test is the reported one: bubbles outside the app off, inside the app on,
 * during an audio/video engagement.
 */
@RunWith(RobolectricTestRunner::class)
internal class ChatBubbleIntegrationTest {

    private val engagementState: PublishProcessor<State> = PublishProcessor.create()

    private lateinit var chatHeadManager: ChatHeadManager
    private lateinit var controller: ChatBubbleController
    private lateinit var watcher: ActivityWatcherForChatHead
    private lateinit var activity: Activity

    /** The reported configuration by default; the overlay tests below turn these on. */
    private var enableBubbleOutsideApp: Boolean = false
    private var hasOverlayPermission: Boolean = false

    @Before
    fun setUp() {
        Logger.mock()
        mockkStatic(GLIA_LOGGER_PATH)
        Dependencies.mock()

        val application = RuntimeEnvironment.getApplication()
        Dependencies.resourceProvider = ResourceProvider(application)
        val localeProvider = mock<LocaleProvider>()
        whenever(localeProvider.getLocaleObservable()) doReturn RxObservable.never()
        whenever(localeProvider.getString(any<Int>(), any<List<StringKeyPair>>())) doReturn "Bubble"
        Dependencies.localeProvider = localeProvider

        val secureConversations = mockk<SecureConversations>()
        every { secureConversations.subscribeToUnreadMessageCount(any()) } just runs
        every { secureConversations.unSubscribeFromUnreadMessageCount(any()) } just runs
        every { Dependencies.secureConversations } returns secureConversations

        val configurationManager = mockk<ConfigurationManager>()
        every { configurationManager.enableBubbleOutsideApp } answers { enableBubbleOutsideApp }
        every { configurationManager.enableBubbleInsideApp } returns true

        val permissionManager = mockk<PermissionManager>()
        every { permissionManager.hasOverlayPermission() } answers { hasOverlayPermission }

        val isQueueingOrLiveEngagementUseCase = mockk<IsQueueingOrLiveEngagementUseCase>()
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        val engagementTypeUseCase = mockk<EngagementTypeUseCase>()
        every { engagementTypeUseCase.isMediaEngagement } returns true
        every { engagementTypeUseCase.hasMedia } returns true
        every { engagementTypeUseCase.isCallVisualizer } returns false

        val engagementStateUseCase = mockk<EngagementStateUseCase>()
        every { engagementStateUseCase() } returns engagementState
        val currentOperatorUseCase = mockk<CurrentOperatorUseCase>()
        every { currentOperatorUseCase() } returns PublishProcessor.create<Operator>()
        val visitorMediaUseCase = mockk<VisitorMediaUseCase>()
        every { visitorMediaUseCase.onHoldState } returns PublishProcessor.create()

        val messagesNotSeenHandler = mockk<MessagesNotSeenHandler>()
        every { messagesNotSeenHandler.addListener(any()) } just runs

        val isCallVisualizer = mockk<IsCurrentEngagementCallVisualizerUseCase>()
        every { isCallVisualizer() } returns false

        chatHeadManager = mockk(relaxUnitFun = true)

        controller = ChatBubbleController(
            decideBubbleRenderTargetUseCase = DecideBubbleRenderTargetUseCase(
                permissionManager,
                configurationManager,
                isQueueingOrLiveEngagementUseCase,
                engagementTypeUseCase
            ),
            resolveChatHeadNavigationUseCase = mockk<ResolveChatHeadNavigationUseCase>(),
            isCurrentEngagementCallVisualizerUseCase = isCallVisualizer,
            isFromCallScreenUseCase = mockk<IsFromCallScreenUseCase>(),
            updateFromCallScreenUseCase = mockk<UpdateFromCallScreenUseCase>(relaxUnitFun = true),
            chatHeadManager = chatHeadManager,
            messagesNotSeenHandler = messagesNotSeenHandler,
            engagementStateUseCase = engagementStateUseCase,
            currentOperatorUseCase = currentOperatorUseCase,
            visitorMediaUseCase = visitorMediaUseCase
        )

        val controllerFactory = mock<ControllerFactory>()
        whenever(controllerFactory.chatBubbleController) doReturn controller
        Dependencies.controllerFactory = controllerFactory
        Dependencies.gliaCore = mockk { every { isInitialized } returns true }

        watcher = ActivityWatcherForChatHead(controller, mockk<ActivityLauncher>(relaxed = true))
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
    }

    @After
    fun tearDown() {
        Logger.unMock()
        unmockkStatic(GLIA_LOGGER_PATH)
        Dependencies.unMock()
        Dependencies.gliaCore = GliaCoreImpl()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private val hasBubble: Boolean
        get() = (activity.rootView as ViewGroup).hasChildOfType(ChatHeadLayout::class.java)

    @Test
    fun `in app bubble shows on a non Glia screen during a media engagement`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))

        // When
        watcher.onActivityResumed(activity)
        idle()

        // Then
        assertTrue(hasBubble)
    }

    @Test
    fun `in app bubble comes back after the app is backgrounded and reopened`() {
        // Given the reported sequence: media engagement, bubble on the main screen
        engagementState.onNext(State.EngagementStarted(false))
        watcher.onActivityResumed(activity)
        idle()
        assertTrue("bubble should be showing before backgrounding", hasBubble)

        // When the app goes to the background
        watcher.onActivityPaused(activity)
        controller.onContextEvent(BubbleContextEvent.AppBackgrounded)
        idle()
        assertFalse("no in-app bubble while the app is in the background", hasBubble)

        // ...and is reopened
        watcher.onActivityResumed(activity)
        idle()

        // Then
        assertTrue(hasBubble)
    }

    @Test
    fun `in app bubble comes back when the app is reopened before the background event arrives`() {
        // Given ProcessLifecycleOwner delays ON_STOP, so a quick return skips AppBackgrounded entirely
        engagementState.onNext(State.EngagementStarted(false))
        watcher.onActivityResumed(activity)
        idle()

        // When
        watcher.onActivityPaused(activity)
        idle()
        watcher.onActivityResumed(activity)
        idle()

        // Then
        assertTrue(hasBubble)
    }

    @Test
    fun `overlay service is never started when bubbles outside the app are disabled`() {
        // Given
        engagementState.onNext(State.EngagementStarted(false))
        watcher.onActivityResumed(activity)
        controller.onContextEvent(BubbleContextEvent.AppBackgrounded)
        idle()

        // Then
        verify(exactly = 0) { chatHeadManager.startChatHeadService() }
    }

    @Test
    fun `the position the visitor dragged the in app bubble to is handed over for the overlay`() {
        // Given a bubble dragged all the way to its top-left limit
        engagementState.onNext(State.EngagementStarted(false))
        watcher.onActivityResumed(activity)
        idle()
        bubble().findViewById<ChatHeadView>(R.id.chat_head_view).drag(dx = -10_000f, dy = -10_000f)

        // Then the shared position both hosts read is the fraction of the drag range, so the overlay
        // can map it into its own, larger surface
        assertEquals(BubblePosition(0f, 0f), controller.bubblePosition)
    }

    // region Overlay allowed as well: the two layers hand over instead of excluding each other

    @Test
    fun `the in app bubble is the visible layer while foregrounded even though the overlay is allowed`() {
        // Given
        enableBubbleOutsideApp = true
        hasOverlayPermission = true

        // When
        engagementState.onNext(State.EngagementStarted(false))
        watcher.onActivityResumed(activity)
        idle()

        // Then the app draws the bubble, and the service is already running for when it cannot
        assertTrue(hasBubble)
        assertEquals(BubbleRenderTarget.APPLICATION, controller.uiState.value.target)
        verify { chatHeadManager.startChatHeadService() }
    }

    @Test
    fun `backgrounding hands the bubble to the overlay without restarting the service`() {
        // Given
        enableBubbleOutsideApp = true
        hasOverlayPermission = true
        engagementState.onNext(State.EngagementStarted(false))
        watcher.onActivityResumed(activity)
        idle()

        // When
        watcher.onActivityPaused(activity)
        controller.onContextEvent(BubbleContextEvent.AppBackgrounded)
        idle()

        // Then the service that was started while foregrounded is the one that draws it
        assertFalse(hasBubble)
        assertEquals(BubbleRenderTarget.SERVICE, controller.uiState.value.target)
        verify(exactly = 0) { chatHeadManager.stopChatHeadService() }
    }

    @Test
    fun `returning to the foreground hands the bubble back to the app`() {
        // Given
        enableBubbleOutsideApp = true
        hasOverlayPermission = true
        engagementState.onNext(State.EngagementStarted(false))
        watcher.onActivityResumed(activity)
        idle()
        watcher.onActivityPaused(activity)
        controller.onContextEvent(BubbleContextEvent.AppBackgrounded)
        idle()

        // When
        watcher.onActivityResumed(activity)
        idle()

        // Then
        assertTrue(hasBubble)
        assertEquals(BubbleRenderTarget.APPLICATION, controller.uiState.value.target)
        verify(exactly = 0) { chatHeadManager.stopChatHeadService() }
    }

    // endregion

    private fun bubble(): ChatHeadLayout =
        (activity.rootView as ViewGroup).let { root ->
            (0 until root.childCount).map(root::getChildAt).filterIsInstance<ChatHeadLayout>().first()
        }
}
