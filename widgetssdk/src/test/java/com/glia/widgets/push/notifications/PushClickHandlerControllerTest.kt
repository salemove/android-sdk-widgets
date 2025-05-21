package com.glia.widgets.push.notifications

import android.mock
import android.unMock
import com.glia.androidsdk.visitor.Visitor
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.dialog.UiComponentsDispatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class PushClickHandlerControllerTest {
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var uiComponentsDispatcher: UiComponentsDispatcher
    private lateinit var gliaCore: GliaCore

    private lateinit var pushClickHandlerController: PushClickHandlerController

    @Before
    fun setup() {
        Logger.mock()

        configurationManager = mockk(relaxUnitFun = true)
        uiComponentsDispatcher = mockk(relaxUnitFun = true)
        gliaCore = mockk(relaxUnitFun = true)

        pushClickHandlerController = PushClickHandlerControllerImpl(
            configurationManager,
            uiComponentsDispatcher,
            gliaCore
        )
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

    @Test
    fun `onAuthenticated does nothing when the pendingPn is null`() {
        pushClickHandlerController.onAuthenticated()

        verify(exactly = 0) { gliaCore.getCurrentVisitor(any(), any()) }
        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchSCTranscriptActivity() }
    }

    @Test
    fun `onAuthenticated does nothing when getting visitor is failed`() {
        val visitorID = "visitorID"
        val queueId = "queueId"
        val onResultSlot = slot<(Visitor) -> Unit>()
        val onErrorSlot = slot<() -> Unit>()

        pushClickHandlerController.handlePushClick(queueId, visitorID)
        pushClickHandlerController.onAuthenticated()

        verify { gliaCore.getCurrentVisitor(capture(onResultSlot), capture(onErrorSlot)) }

        onErrorSlot.captured.invoke()

        //call onAuthenticated multiple times to check that it doesn't call getCurrentVisitor again and pendingPn is null
        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any(), any()) }

        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchSCTranscriptActivity() }
    }

    @Test
    fun `onAuthenticated logs when the auth is failed`() {
        val visitorID = "visitorID"
        val queueId = "queueId"
        val visitor = mockk<Visitor> {
            every { visitorId } returns visitorID
            every { isVisitorAuthenticated } returns false
        }
        val onResultSlot = slot<(Visitor) -> Unit>()

        pushClickHandlerController.handlePushClick(queueId, visitorID)
        pushClickHandlerController.onAuthenticated()

        verify { gliaCore.getCurrentVisitor(capture(onResultSlot), any()) }

        onResultSlot.captured(visitor)

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
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any(), any()) }

        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchSCTranscriptActivity() }
    }

    @Test
    fun `onAuthenticated logs when the visitor ids does not match`() {
        val visitorID = "visitorID"
        val queueId = "queueId"
        val visitor = mockk<Visitor> {
            every { visitorId } returns "anothervisitorID"
            every { isVisitorAuthenticated } returns true
        }
        val onResultSlot = slot<(Visitor) -> Unit>()

        pushClickHandlerController.handlePushClick(queueId, visitorID)
        pushClickHandlerController.onAuthenticated()

        verify { gliaCore.getCurrentVisitor(capture(onResultSlot), any()) }

        onResultSlot.captured(visitor)

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
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any(), any()) }

        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchSCTranscriptActivity() }
    }

    @Test
    fun `onAuthenticated launches sc transcript activity when visitor ids match`() {
        val visitorID = "visitorID"
        val queueId = "queueId"
        val visitor = mockk<Visitor> {
            every { visitorId } returns visitorID
            every { isVisitorAuthenticated } returns true
        }
        val onResultSlot = slot<(Visitor) -> Unit>()

        pushClickHandlerController.handlePushClick(queueId, visitorID)
        pushClickHandlerController.onAuthenticated()

        verify { gliaCore.getCurrentVisitor(capture(onResultSlot), any()) }

        onResultSlot.captured(visitor)

        verify { configurationManager.setQueueIds(any()) }
        verify { uiComponentsDispatcher.launchSCTranscriptActivity() }
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
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any(), any()) }

        verify(exactly = 1) { configurationManager.setQueueIds(listOf(queueId)) }
        verify(exactly = 1) { uiComponentsDispatcher.launchSCTranscriptActivity() }
    }

    @Test
    fun `onAuthenticated creates empty list when the queue id is null`() {
        val visitorID = "visitorID"
        val queueId = null
        val visitor = mockk<Visitor> {
            every { visitorId } returns visitorID
            every { isVisitorAuthenticated } returns true
        }
        val onResultSlot = slot<(Visitor) -> Unit>()

        pushClickHandlerController.handlePushClick(queueId, visitorID)
        pushClickHandlerController.onAuthenticated()

        verify { gliaCore.getCurrentVisitor(capture(onResultSlot), any()) }

        onResultSlot.captured(visitor)

        verify { configurationManager.setQueueIds(any()) }
        verify { uiComponentsDispatcher.launchSCTranscriptActivity() }
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
        verify(exactly = 1) { gliaCore.getCurrentVisitor(any(), any()) }

        verify(exactly = 1) { configurationManager.setQueueIds(match { it.isEmpty() }) }
        verify(exactly = 1) { uiComponentsDispatcher.launchSCTranscriptActivity() }
    }

}
