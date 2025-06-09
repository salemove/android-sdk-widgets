package com.glia.widgets.push.notifications

import android.mock
import android.unMock
import com.glia.androidsdk.visitor.Visitor
import com.glia.widgets.chat.Intention
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.di.GliaCore
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.dialog.UiComponentsDispatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class PushClickHandlerControllerTest {
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var uiComponentsDispatcher: UiComponentsDispatcher
    private lateinit var gliaCore: GliaCore
    private lateinit var isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase

    private lateinit var pushClickHandlerController: PushClickHandlerController

    @Before
    fun setup() {
        Logger.mock()

        configurationManager = mockk(relaxUnitFun = true)
        uiComponentsDispatcher = mockk(relaxUnitFun = true)
        gliaCore = mockk(relaxUnitFun = true)
        isQueueingOrLiveEngagementUseCase = mockk(relaxUnitFun = true)
        isAuthenticatedUseCase = mockk(relaxUnitFun = true)

        pushClickHandlerController = PushClickHandlerControllerImpl(
            configurationManager,
            uiComponentsDispatcher,
            gliaCore,
            isQueueingOrLiveEngagementUseCase,
            isAuthenticatedUseCase
        )
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

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
    fun `onAuthenticated does nothing when no engagement and not authenticated yet`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false

        pushClickHandlerController.onAuthenticated()

        verify(exactly = 0) { gliaCore.getCurrentVisitor(any()) }
        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `onAuthenticated logs when the auth is failed`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        val queueId = "queueId"
        mockkGetCurrentVisitorCallback(visitorId, false)
        pushClickHandlerController.handlePushClick(queueId, visitorId)
        pushClickHandlerController.onAuthenticated()

        verify { gliaCore.getCurrentVisitor(any()) }

        verify {
            Logger.i(
                any(),
                "Secure message push with unauthenticated visitor is processed with no actions."
            )
        }

        //call onAuthenticated multiple times to check that it doesn't call getCurrentVisitor again and pendingPn is null
        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any()) }

        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `onAuthenticated logs when the visitor ids does not match`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        val queueId = "queueId"
        mockkGetCurrentVisitorCallback(visitorId, true)

        pushClickHandlerController.handlePushClick(queueId, "anotherVisitorId")
        pushClickHandlerController.onAuthenticated()

        verify { gliaCore.getCurrentVisitor( any()) }

        verify {
            Logger.i(
                any(),
                "Secure message push with unmatching `visitor_id` processed with no actions."
            )
        }

        //call onAuthenticated multiple times to check that it doesn't call getCurrentVisitor again and pendingPn is null
        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()
        verify(exactly = 1) { gliaCore.getCurrentVisitor( any()) }

        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `onAuthenticated launches sc transcript activity when visitor ids match`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        val queueId = "queueId"
        mockkGetCurrentVisitorCallback(visitorId, true)
        pushClickHandlerController.handlePushClick(queueId, visitorId)
        pushClickHandlerController.onAuthenticated()

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
        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any()) }

        verify(exactly = 1) { configurationManager.setQueueIds(listOf(queueId)) }
        verify(exactly = 1) { uiComponentsDispatcher.launchChatScreen(any()) }
    }

    @Test
    fun `onAuthenticated creates empty list when the queue id is null`() {
        every { isAuthenticatedUseCase() } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns false
        val visitorId = "visitorId"
        val queueId = null
        mockkGetCurrentVisitorCallback(visitorId, true)
        pushClickHandlerController.handlePushClick(queueId, visitorId)
        pushClickHandlerController.onAuthenticated()

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
        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any()) }

        verify(exactly = 1) { configurationManager.setQueueIds(match { it.isEmpty() }) }
        verify(exactly = 1) { uiComponentsDispatcher.launchChatScreen(any()) }
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
