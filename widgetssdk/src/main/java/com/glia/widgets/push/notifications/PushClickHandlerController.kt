package com.glia.widgets.push.notifications

import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.dialog.UiComponentsDispatcher

internal interface PushClickHandlerController {
    fun handlePushClick(queueId: String?)
    fun onAuthenticated()
}

internal class PushClickHandlerControllerImpl(
    private val configurationManager: ConfigurationManager,
    private val uiComponentsDispatcher: UiComponentsDispatcher
) : PushClickHandlerController {

    // Presence of this queue list either with queue or empty indicates that we're waiting for authentication
    private var pendingPushQueues: List<String>? = null

    override fun handlePushClick(queueId: String?) {
        // If the queueId is null, the empty list will be assigned to the pendingPushQueues
        pendingPushQueues = listOfNotNull(queueId)
    }

    override fun onAuthenticated() {
        configurationManager.setQueueIds(pendingPushQueues ?: return)
        uiComponentsDispatcher.launchSCTranscriptActivity()
        pendingPushQueues = null
    }

}
