package com.glia.widgets.push.notifications

import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.dialog.UiComponentsDispatcher
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class PushClickHandlerControllerTest {
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var uiComponentsDispatcher: UiComponentsDispatcher

    private lateinit var pushClickHandlerController: PushClickHandlerController

    @Before
    fun setup() {
        configurationManager = mockk(relaxUnitFun = true)
        uiComponentsDispatcher = mockk(relaxUnitFun = true)

        pushClickHandlerController = PushClickHandlerControllerImpl(
            configurationManager,
            uiComponentsDispatcher
        )
    }

    @Test
    fun `onAuthenticated does nothing when pendingPushQueues is null`() {
        pushClickHandlerController.onAuthenticated()

        verify(exactly = 0) { configurationManager.setQueueIds(any()) }
        verify(exactly = 0) { uiComponentsDispatcher.launchSCTranscriptActivity() }
    }

    @Test
    fun `onAuthenticated sets queue ids and launches SCTranscriptActivity when pendingPushQueues is not null`() {
        val queueId = "queueId"
        pushClickHandlerController.handlePushClick(queueId)

        pushClickHandlerController.onAuthenticated()

        verify { configurationManager.setQueueIds(listOf(queueId)) }
        verify { uiComponentsDispatcher.launchSCTranscriptActivity() }
    }


    @Test
    fun `onAuthenticated resets the pendingPushQueues when the activity is launched`() {
        val queueId = "queueId"
        pushClickHandlerController.handlePushClick(queueId)

        pushClickHandlerController.onAuthenticated()
        pushClickHandlerController.onAuthenticated()

        verify(exactly = 1) { configurationManager.setQueueIds(any()) }
        verify(exactly = 1) { uiComponentsDispatcher.launchSCTranscriptActivity() }
    }

    @Test
    fun `handlePushClick sets empty list when the queue id is null`() {
        pushClickHandlerController.handlePushClick(null)

        pushClickHandlerController.onAuthenticated()

        verify { configurationManager.setQueueIds(match { it.isEmpty() }) }
        verify { uiComponentsDispatcher.launchSCTranscriptActivity() }
    }

}
