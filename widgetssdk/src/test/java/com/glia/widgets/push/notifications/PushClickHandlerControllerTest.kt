package com.glia.widgets.push.notifications

import android.GLIA_LOGGER_PATH
import android.mock
import android.os.Bundle
import android.unMock
import com.glia.androidsdk.visitor.Visitor
import com.glia.widgets.chat.Intention
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.di.GliaCore
import com.glia.widgets.engagement.EndAction
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.dialog.UiComponentsDispatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import com.glia.androidsdk.fcm.GliaPushMessage.PushType as CorePushType
import com.glia.androidsdk.fcm.PushNotifications as CorePushNotifications

class PushClickHandlerControllerTest {
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var uiComponentsDispatcher: UiComponentsDispatcher
    private lateinit var gliaCore: GliaCore
    private lateinit var isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var engagementStateUseCase: EngagementStateUseCase
    private lateinit var corePushNotifications: CorePushNotifications

    private lateinit var pushClickHandlerController: PushClickHandlerController

    @Before
    fun setup() {
        Logger.mock()
        mockkStatic(GLIA_LOGGER_PATH)

        configurationManager = mockk(relaxUnitFun = true)
        uiComponentsDispatcher = mockk(relaxUnitFun = true)
        corePushNotifications = mockk(relaxed = true)
        gliaCore = mockk(relaxUnitFun = true) {
            every { isInitialized } returns true
            every { pushNotifications } returns corePushNotifications
        }
        isQueueingOrLiveEngagementUseCase = mockk(relaxUnitFun = true)
        isAuthenticatedUseCase = mockk(relaxUnitFun = true)
        engagementStateUseCase = mockk(relaxUnitFun = true) {
            every { this@mockk.invoke() } returns Flowable.never()
        }

        pushClickHandlerController = buildController()
    }

    @After
    fun tearDown() {
        Logger.unMock()
        unmockkStatic(GLIA_LOGGER_PATH)
        RxJavaPlugins.reset()
    }

    private fun buildController(): PushClickHandlerController = PushClickHandlerControllerImpl(
        configurationManager,
        uiComponentsDispatcher,
        gliaCore,
        isQueueingOrLiveEngagementUseCase,
        isAuthenticatedUseCase,
        engagementStateUseCase
    )

    @Test
    fun `handlePushClick will just log when ongoing engagement but visitor is not authenticated`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        val visitorId = "visitorId"
        val queueId = "queueId"

        mockkGetCurrentVisitorCallback(visitorId, false)

        pushClickHandlerController.handlePushClick(queueId, visitorId)

        verify { gliaCore.getCurrentVisitor(any()) }
        verify {
            Logger.i(any(), "Secure message push with unauthenticated visitor is processed with no actions.")
        }

        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `handlePushClick will just log when authenticated with different visitor`() {
        every { isAuthenticatedUseCase() } returns true
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        val queueId = "queueId"

        mockkGetCurrentVisitorCallback(visitorId, true)

        pushClickHandlerController.handlePushClick(queueId, "anotherVisitorId")

        verify { gliaCore.getCurrentVisitor(any()) }

        verify {
            Logger.i(any(), "Secure message push with unmatching `visitor_id` processed with no actions.")
        }
        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `handlePushClick will open SC transcript screen when authenticated but no ongoing engagement`() {
        every { isAuthenticatedUseCase() } returns true
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        val queueId = "queueId"

        mockkGetCurrentVisitorCallback(visitorId, true)

        pushClickHandlerController.handlePushClick(queueId, visitorId)

        verify { gliaCore.getCurrentVisitor(any()) }

        verify { configurationManager.setQueueIds(listOf(queueId)) }
        verify { uiComponentsDispatcher.launchChatScreen(Intention.SC_CHAT) }
        verify {
            Logger.i(any(), "Secure message push with matching `visitor_id` and authenticated visitor is processed to open chat transcript.")
        }
    }

    @Test
    fun `handlePushClick will open Live Chat screen when authenticated and has ongoing engagement`() {
        every { isAuthenticatedUseCase() } returns true
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        val visitorId = "visitorId"
        val queueId = "queueId"

        mockkGetCurrentVisitorCallback(visitorId, true)

        pushClickHandlerController.handlePushClick(queueId, visitorId)

        verify { gliaCore.getCurrentVisitor(any()) }

        verify { configurationManager.setQueueIds(listOf(queueId)) }
        verify { uiComponentsDispatcher.launchChatScreen(Intention.RETURN_TO_CHAT) }
        verify {
            Logger.i(any(), "Secure message push with matching `visitor_id` and authenticated visitor is processed to open chat transcript.")
        }
    }

    @Test
    fun `onAuthenticationAttempt does nothing when no engagement and not authenticated yet`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false

        pushClickHandlerController.onAuthenticationAttempt()

        verify(exactly = 0) { gliaCore.getCurrentVisitor(any()) }
        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `onAuthenticationAttempt logs when the auth is failed`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        val queueId = "queueId"
        mockkGetCurrentVisitorCallback(visitorId, false)
        pushClickHandlerController.handlePushClick(queueId, visitorId)
        pushClickHandlerController.onAuthenticationAttempt()

        verify { gliaCore.getCurrentVisitor(any()) }

        verify {
            Logger.i(
                any(),
                "Secure message push with unauthenticated visitor is processed with no actions."
            )
        }

        //call onAuthenticated multiple times to check that it doesn't call getCurrentVisitor again and pendingPn is null
        pushClickHandlerController.onAuthenticationAttempt()
        pushClickHandlerController.onAuthenticationAttempt()
        pushClickHandlerController.onAuthenticationAttempt()
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any()) }

        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `onAuthenticationAttempt logs when the visitor ids does not match`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        val queueId = "queueId"
        mockkGetCurrentVisitorCallback(visitorId, true)

        pushClickHandlerController.handlePushClick(queueId, "anotherVisitorId")
        pushClickHandlerController.onAuthenticationAttempt()

        verify { gliaCore.getCurrentVisitor(any()) }

        verify {
            Logger.i(
                any(),
                "Secure message push with unmatching `visitor_id` processed with no actions."
            )
        }

        //call onAuthenticated multiple times to check that it doesn't call getCurrentVisitor again and pendingPn is null
        pushClickHandlerController.onAuthenticationAttempt()
        pushClickHandlerController.onAuthenticationAttempt()
        pushClickHandlerController.onAuthenticationAttempt()
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any()) }

        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `onAuthenticationAttempt launches sc transcript activity when visitor ids match`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        val queueId = "queueId"
        mockkGetCurrentVisitorCallback(visitorId, true)
        pushClickHandlerController.handlePushClick(queueId, visitorId)
        pushClickHandlerController.onAuthenticationAttempt()

        verify { gliaCore.getCurrentVisitor(any()) }

        verify { configurationManager.setQueueIds(any()) }
        verify { uiComponentsDispatcher.launchChatScreen(Intention.SC_CHAT) }
        verify {
            Logger.i(
                any(),
                "Secure message push with matching `visitor_id` and authenticated visitor is processed to open chat transcript."
            )
        }

        //call onAuthenticated multiple times to check that it doesn't call getCurrentVisitor again and pendingPn is null
        pushClickHandlerController.onAuthenticationAttempt()
        pushClickHandlerController.onAuthenticationAttempt()
        pushClickHandlerController.onAuthenticationAttempt()
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any()) }

        verify(exactly = 1) { configurationManager.setQueueIds(listOf(queueId)) }
        verify(exactly = 1) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `onAuthenticationAttempt creates empty list when the queue id is null`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        val queueId = null
        mockkGetCurrentVisitorCallback(visitorId, true)
        pushClickHandlerController.handlePushClick(queueId, visitorId)
        pushClickHandlerController.onAuthenticationAttempt()

        verify { gliaCore.getCurrentVisitor(any()) }

        verify { configurationManager.setQueueIds(any()) }
        verify { uiComponentsDispatcher.launchChatScreen(any()) }
        verify {
            Logger.i(
                any(),
                "Secure message push with matching `visitor_id` and authenticated visitor is processed to open chat transcript."
            )
        }

        //call onAuthenticated multiple times to check that it doesn't call getCurrentVisitor again and pendingPn is null
        pushClickHandlerController.onAuthenticationAttempt()
        pushClickHandlerController.onAuthenticationAttempt()
        pushClickHandlerController.onAuthenticationAttempt()
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any()) }

        verify(exactly = 1) { configurationManager.setQueueIds(match { it.isEmpty() }) }
        verify(exactly = 1) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    // region handlePushNotificationClick - type routing

    @Test
    fun `handlePushNotificationClick returns to the engagement for a chat message push`() {
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.CHAT_MESSAGE))

        verify { uiComponentsDispatcher.launchChatScreen(Intention.RETURN_TO_CHAT) }
    }

    @Test
    fun `handlePushNotificationClick opens the transcript for a queued message push when authenticated`() {
        every { isAuthenticatedUseCase() } returns true

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.QUEUED_MESSAGE))

        verify { uiComponentsDispatcher.launchChatScreen(Intention.SC_CHAT) }
    }

    @Test
    fun `handlePushNotificationClick does nothing for a secure conversation push`() {
        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.SECURE_CONVERSATION))

        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `handlePushNotificationClick does nothing for an unidentified push`() {
        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.UNIDENTIFIED))

        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `handlePushNotificationClick does nothing when core does not recognise the bundle`() {
        val bundle = mockk<Bundle>(relaxed = true)
        every { corePushNotifications.parsePushMessageType(bundle) } returns null

        pushClickHandlerController.handlePushNotificationClick(bundle)

        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    // endregion

    // region handlePushNotificationClick - initialization gate

    @Test
    fun `handlePushNotificationClick parks the click until the SDK is initialized`() {
        every { gliaCore.isInitialized } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        pushClickHandlerController = buildController()

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.CHAT_MESSAGE))
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }

        pushClickHandlerController.onSdkInitialized()
        verify { uiComponentsDispatcher.launchChatScreen(Intention.RETURN_TO_CHAT) }
    }

    @Test
    fun `onSdkInitialized called twice launches the parked click once`() {
        every { gliaCore.isInitialized } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        pushClickHandlerController = buildController()

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.CHAT_MESSAGE))
        pushClickHandlerController.onSdkInitialized()
        pushClickHandlerController.onSdkInitialized()

        verify(exactly = 1) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `handlePushNotificationClick does not wait when the SDK is already initialized`() {
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.CHAT_MESSAGE))

        verify { uiComponentsDispatcher.launchChatScreen(Intention.RETURN_TO_CHAT) }
    }

    // endregion

    // region handlePushNotificationClick - authentication gate

    @Test
    fun `queued message push parks until the visitor is authenticated`() {
        every { isAuthenticatedUseCase() } returns false

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.QUEUED_MESSAGE))
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }

        every { isAuthenticatedUseCase() } returns true
        pushClickHandlerController.onAuthenticationAttempt()

        verify { uiComponentsDispatcher.launchChatScreen(Intention.SC_CHAT) }
    }

    @Test
    fun `onAuthenticationAttempt flushes both a parked secure conversation click and a parked engagement click`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        mockkGetCurrentVisitorCallback(visitorId, true)

        pushClickHandlerController.handlePushClick("queueId", visitorId)
        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.QUEUED_MESSAGE))

        every { isAuthenticatedUseCase() } returns true
        pushClickHandlerController.onAuthenticationAttempt()

        verify { gliaCore.getCurrentVisitor(any()) }
        verify(exactly = 2) { uiComponentsDispatcher.launchChatScreen(Intention.SC_CHAT) }
    }

    // endregion

    // region handlePushNotificationClick - engagement gate

    @Test
    fun `chat message push waits for the engagement before launching`() {
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val engagementState = BehaviorProcessor.createDefault<State>(State.NoEngagement)
        every { engagementStateUseCase() } returns engagementState

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.CHAT_MESSAGE))
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }

        engagementState.onNext(State.EngagementStarted(false))

        verify { uiComponentsDispatcher.launchChatScreen(Intention.RETURN_TO_CHAT) }
    }

    @Test
    fun `chat message push launches once across further engagement state updates`() {
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val engagementState = BehaviorProcessor.createDefault<State>(State.NoEngagement)
        every { engagementStateUseCase() } returns engagementState

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.CHAT_MESSAGE))
        engagementState.onNext(State.EngagementStarted(false))
        engagementState.onNext(State.EngagementStarted(false))
        engagementState.onNext(State.EngagementEnded(EndAction.Retain))

        verify(exactly = 1) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `chat message push is dropped when no engagement is restored in time`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val engagementState = BehaviorProcessor.createDefault<State>(State.NoEngagement)
        every { engagementStateUseCase() } returns engagementState

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.CHAT_MESSAGE))
        testScheduler.advanceTimeBy(ENGAGEMENT_RESTORE_TIMEOUT_SEC, TimeUnit.SECONDS)

        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
        verify { Logger.i(any(), "No engagement was restored in time, the push notification is treated as stale.") }

        // A push tap must never start an engagement, so a late restore opens nothing either
        engagementState.onNext(State.EngagementStarted(false))
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `queued message push does not wait for an engagement`() {
        every { isAuthenticatedUseCase() } returns true
        every { engagementStateUseCase() } returns Flowable.never()

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.QUEUED_MESSAGE))

        verify { uiComponentsDispatcher.launchChatScreen(Intention.SC_CHAT) }
    }

    // endregion

    // region handlePushNotificationClick - idempotency

    @Test
    fun `the same notification handled twice launches once`() {
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        val bundle = pushBundle(CorePushType.CHAT_MESSAGE, messageId = "0:1")

        pushClickHandlerController.handlePushNotificationClick(bundle)
        pushClickHandlerController.handlePushNotificationClick(bundle)

        verify(exactly = 1) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `two different notifications both launch`() {
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true

        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.CHAT_MESSAGE, messageId = "0:1"))
        pushClickHandlerController.handlePushNotificationClick(pushBundle(CorePushType.CHAT_MESSAGE, messageId = "0:2"))

        verify(exactly = 2) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    // endregion

    private fun pushBundle(type: CorePushType, messageId: String = "0:1600000000000000%abcdef"): Bundle {
        val bundle = mockk<Bundle> {
            every { getString("google.message_id") } returns messageId
        }
        every { corePushNotifications.parsePushMessageType(bundle) } returns type
        return bundle
    }

    private fun mockkGetCurrentVisitorCallback(id: String, authenticated: Boolean) {
        val visitor = mockk<Visitor> {
            every { visitorId } returns id
            every { isVisitorAuthenticated } returns authenticated
        }

        every { gliaCore.getCurrentVisitor(captureLambda()) } answers {
            firstArg<(Visitor) -> Unit>().invoke(visitor)
        }
    }

}
